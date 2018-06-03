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
import { ws } from "../../services/fetchUtil"
import { coin as createCoin } from "../coin/reducer"
import * as coinActions from "../coin/actions"
import * as routerActionTypes from "../router/actionTypes"
import * as errorActions from "../error/actions"
import * as notificationActions from "../notifications/actions"
import { getSelectedCoin, locationToCoin } from "../../selectors/coins"
import { augmentCoin } from "../coin/reducer"
import * as channelMessages from "../../worker/socketMessages"

const serverMessages = {
  READY: "READY",
  TICKER: "TICKER",
  OPEN_ORDERS: "OPEN_ORDERS",
  ORDERBOOK: "ORDERBOOK",
  TRADE_HISTORY: "TRADE_HISTORY",
  ERROR: "ERROR",
  CHANGE_TICKERS: "CHANGE_TICKERS",
  CHANGE_OPEN_ORDERS: "CHANGE_OPEN_ORDERS",
  CHANGE_ORDER_BOOK: "CHANGE_ORDER_BOOK",
  CHANGE_TRADE_HISTORY: "CHANGE_TRADE_HISTORY",
  UPDATE_SUBSCRIPTIONS: "UPDATE_SUBSCRIPTIONS",
  NOTIFICATION: "NOTIFICATION"
}

const workerCode = () => {
  var socket
  let onmessage = m => {
    console.log(m)
    switch (m.data.action) {
      case channelMessages.CONNECT:
        socket = ws(m.data.data)
        socket.onopen = () => postMessage(channelMessages.OPEN)
        socket.onclose = () => postMessage(channelMessages.CLOSE)
        socket.onmessage = evt => {
          try {
              postMessage(JSON.parse(evt.data))
          } catch (e) {
              console.log("Invalid message from server", evt.data)
          }
        }
        break
      case channelMessages.DISCONNECT:
        socket.close(undefined, "Shutdown", { keepClosed: true })
        break
      default:
        console.log("Unknown message", m)
    }
  }
}

const getAuth = state => state.auth
const getSubscribedCoins = state => state.coins.coins

/**
 * Event channel which allows the saga to react to incoming
 * messages on the socket, or socket open/close in between
 * retries
 */
function socketMessageChannel(worker) {
  return eventChannel(emit => {
    worker.onmessage = m => {
      try {
        emit(JSON.parse(m))
      } catch (e) {
        console.log("Invalid message from server", m)
      }
    }
    return () => {}
  })
}

function* socketLoop(socketChannel) {
  const message = yield take(socketChannel)
  if (message === channelMessages.OPEN) {
    console.log("Socket (re)connected")
    yield put.resolve({ type: types.SET_CONNECTION_STATE, connected: true })
    yield put({ type: types.RESUBSCRIBE })
  } else if (message && message === channelMessages.CLOSE) {
    console.log("Socket connection temporarily lost")
    yield put({ type: types.SET_CONNECTION_STATE, connected: false })
  } else if (message && message.nature === serverMessages.ERROR) {
    console.log("Error from socket")
    yield put(errorActions.addBackground(message.data, "ws"))
  } else if (message && message.nature === serverMessages.TICKER) {
    yield put(errorActions.clearBackground("ws"))
    yield put({
      type: types.SET_TICKER,
      coin: createCoin(message.data.spec.exchange, message.data.spec.counter, message.data.spec.base),
      ticker: message.data.ticker
    })      
  } else if (message && (message.nature === serverMessages.OPEN_ORDERS || message.nature === serverMessages.ORDERBOOK || message.nature === serverMessages.TRADE_HISTORY)) {

    // Ignore late-arriving messages related to a coin we're not interested in right now
    const selectedCoin = yield select(getSelectedCoin)
    const referredCoin = augmentCoin(message.data.spec)
    if (selectedCoin && selectedCoin.key === referredCoin.key) {
      yield put(errorActions.clearBackground("ws"))
      if (message.nature === serverMessages.OPEN_ORDERS) {
        yield put(coinActions.setOrders(message.data.openOrders))
      } else if (message.nature === serverMessages.ORDERBOOK) {
        yield put(coinActions.setOrderBook(message.data.orderBook))
      } else if (message.nature === serverMessages.TRADE_HISTORY) {
        yield put(coinActions.setTradeHistory(message.data.userTrades))
      }
    }

  } else if (message && message.nature === serverMessages.NOTIFICATION) {
    yield put(notificationActions.add(message.data))
  } else {
    yield put(
      errorActions.addBackground(
        "Unknown message from server: " + JSON.stringify(message),
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

    let code = workerCode.toString();
    code = code.substring(code.indexOf("{")+1, code.lastIndexOf("}"));
    const blob = new Blob([code], {type: "application/javascript"});
    const worker = new Worker(URL.createObjectURL(blob));

    worker.postMessage({ action: channelMessages.CONNECT, data: token })
    const socketChannel = yield call(socketMessageChannel, worker)

    setInterval(() => {
      if (getState().ticker.connected) {
        worker.postMessage(JSON.stringify({command: serverMessages.READY}))
      }
    }, 3000)

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
        socketChannel.close()
        break

      } else if (action.type === types.RESUBSCRIBE || action.type === routerActionTypes.LOCATION_CHANGED) {

        yield put(coinActions.setOrders(null))
        yield put(coinActions.setOrderBook(null))

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

        yield worker.postMessage(JSON.stringify({
          command: serverMessages.CHANGE_TICKERS,
          tickers: coins.map(coin => webCoinToServerCoin(coin))
        }))
        yield worker.postMessage(JSON.stringify({
          command: serverMessages.CHANGE_OPEN_ORDERS,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        }))
        yield worker.postMessage(JSON.stringify({
          command: serverMessages.CHANGE_ORDER_BOOK,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        }))
        yield worker.postMessage(JSON.stringify({
          command: serverMessages.CHANGE_TRADE_HISTORY,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        }))
        yield worker.postMessage(JSON.stringify({ command: serverMessages.UPDATE_SUBSCRIPTIONS }))
     
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