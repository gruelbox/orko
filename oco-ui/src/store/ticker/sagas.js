import * as types from "./actionTypes"
import {
  take,
  call,
  put,
  race,
  all,
  select,
  fork,
  join
} from "redux-saga/effects"
import { eventChannel } from "redux-saga"
import { coin as createCoin } from "../coin/reducer"
import * as coinActions from "../coin/actions"
import * as routerActionTypes from "../router/actionTypes"
import * as errorActions from "../error/actions"
import * as notificationActions from "../notifications/actions"
import { getSelectedCoin, locationToCoin } from "../../selectors/coins"
import { augmentCoin } from "../coin/reducer"
import * as socketEvents from "../../worker/socketEvents"
import * as serverMessages from "../../worker/socketMessages"
import Worker from '../../worker/socket.worker.js'
import runtimeEnv from "@mars/heroku-js-runtime-env"

const getAuth = state => state.auth
const getSubscribedCoins = state => state.coins.coins

/**
 * Event channel which allows the saga to react to incoming
 * messages on the socket, or socket open/close in between
 * retries
 */
function socketMessageChannel(worker) {
  return eventChannel(emit => {
    worker.onmessage = m => emit(JSON.parse(m.data))
    return () => worker.postMessage({ eventType: socketEvents.DISCONNECT })
  })
}

function* socketLoop(socketChannel) {
  const event = yield take(socketChannel)
  if (!event) {

    yield put(errorActions.addBackground("Empty event from server", "ws"))

  } else if (event.eventType === socketEvents.OPEN) {
    
    console.log("Socket (re)connected")
    yield put.resolve({ type: types.SET_CONNECTION_STATE, connected: true })
    yield put({ type: types.RESUBSCRIBE })

  } else if (event.eventType === socketEvents.CLOSE) {

    console.log("Socket connection temporarily lost")
    yield put({ type: types.SET_CONNECTION_STATE, connected: false })

  } else if (event.eventType === socketEvents.MESSAGE) {

    const message = event.payload

    if (message.nature === serverMessages.ERROR) {

      console.log("Error from socket")
      yield put(errorActions.addBackground(message.data, "ws"))

    } else if (message.nature === serverMessages.TICKER) {

      yield put(errorActions.clearBackground("ws"))
      yield put({
        type: types.SET_TICKER,
        coin: createCoin(message.data.spec.exchange, message.data.spec.counter, message.data.spec.base),
        ticker: message.data.ticker
      })      

    } else if (message.nature === serverMessages.OPEN_ORDERS || message.nature === serverMessages.ORDERBOOK || message.nature === serverMessages.TRADE_HISTORY) {

      // Ignore late-arriving messages related to a coin we're not interested in right now
      const selectedCoin = yield select(getSelectedCoin)
      const referredCoin = yield augmentCoin(message.data.spec)
      if (selectedCoin && selectedCoin.key === referredCoin.key) {
        yield put(errorActions.clearBackground("ws"))
        if (message.nature === serverMessages.OPEN_ORDERS) {
          yield put(coinActions.setOrders(message.data.openOrders))
        } else if (message.nature === serverMessages.ORDERBOOK) {
          yield put(coinActions.setOrderBook(message.data.orderBook))
        } else if (message.nature === serverMessages.TRADE_HISTORY) {
          yield put(coinActions.setTradeHistory(message.data.trades))
        }
      }

    } else if (message.nature === serverMessages.NOTIFICATION) {
      yield put(notificationActions.add(message.data))
    }

  } else {
    yield put(
      errorActions.addBackground(
        "Unknown event from server: " + JSON.stringify(event),
        "ws"
      )
    )
  }
}

function* actionLoop() {
  return yield take([
    types.DISCONNECT,
    types.RESUBSCRIBE,
    routerActionTypes.LOCATION_CHANGED
  ])
}

const webCoinToServerCoin = coin => ({
  exchange: coin.exchange,
  counter: coin.counter,
  base: coin.base
})

function* socketManager(dispatch, getState) {
  while (true) {
    const auth = yield select(getAuth)
    if (!auth.token || !auth.whitelisted || !auth.loggedIn) {
      console.log("Saga waiting for connect request...")
      yield take(types.CONNECT)
    }

    console.log("Connecting to socket...")
    const token = (yield select(getAuth)).token
    const worker = new Worker()
    worker.postMessage({ eventType: socketEvents.CONNECT, payload: {
      token,
      root: runtimeEnv().REACT_APP_WS_URL
    }})
    const socketChannel = yield call(socketMessageChannel, worker)
    
    const sendToSocket = message => worker.postMessage({
      eventType: socketEvents.MESSAGE,
      payload: message
    })

    while (true) {

      const socketTask = yield fork(socketLoop, socketChannel)
      const actionTask = yield fork(actionLoop)

      const { action } = yield race({
        socketLoopOutcome: join(socketTask),
        action: join(actionTask)
      })

      if (!action) continue

      if (action.type === types.DISCONNECT) {

        console.log("Disconnecting socket")
        yield socketChannel.close()
        break

      } else if (action.type === types.RESUBSCRIBE || action.type === routerActionTypes.LOCATION_CHANGED) {

        yield put(coinActions.setOrders(null))
        yield put(coinActions.setOrderBook(null))
        yield put(coinActions.setTradeHistory(null))

        var coins = yield select(getSubscribedCoins)
        const selectedCoin = action.type === routerActionTypes.LOCATION_CHANGED
          ? yield locationToCoin(action.location)
          : yield select(getSelectedCoin)
        if (selectedCoin) {
          coins = coins.concat([selectedCoin])
          if (action.type === routerActionTypes.LOCATION_CHANGED) {
            yield dispatch(coinActions.fetchBalance(selectedCoin))
            yield dispatch(coinActions.fetchOrders(selectedCoin))
          }
        }

        yield sendToSocket({
          command: serverMessages.CHANGE_TICKERS,
          tickers: coins.map(coin => webCoinToServerCoin(coin))
        })
        yield sendToSocket({
          command: serverMessages.CHANGE_OPEN_ORDERS,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        })
        yield sendToSocket({
          command: serverMessages.CHANGE_ORDER_BOOK,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        })
        yield sendToSocket({
          command: serverMessages.CHANGE_TRADE_HISTORY,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        })
        yield sendToSocket({ command: serverMessages.UPDATE_SUBSCRIPTIONS })
     
      }
    }
  }
}



/**
 * The saga. Connects a reconnecting websocket and starts the listeners
 * for messages on the channel and outoing messages from redux dispatch.
 */
export function* watcher(dispatch, getState) {
  while (true) {
    yield race({
      task: all([call(socketManager, dispatch, getState)])
    })
    console.log("Started listeners")
  }
}