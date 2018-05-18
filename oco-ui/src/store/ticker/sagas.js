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
import { ws } from "../../services/fetchUtil"
import { eventChannel } from "redux-saga"
import { coin as createCoin } from "../coin/reducer"
import * as coinActions from "../coin/actions"
import * as routerActionTypes from "../router/actionTypes"
import * as errorActions from "../error/actions"
import * as notificationActions from "../notifications/actions"
import { getSelectedCoin, locationToCoin } from "../../selectors/coins"

const channelMessages = {
  OPEN: "OPEN",
  CLOSE: "CLOSE"
}

const serverMessages = {
  TICKER: "TICKER",
  OPEN_ORDERS: "OPEN_ORDERS",
  ERROR: "ERROR",
  CHANGE_TICKERS: "CHANGE_TICKERS",
  CHANGE_OPEN_ORDERS: "CHANGE_OPEN_ORDERS",
  UPDATE_SUBSCRIPTIONS: "UPDATE_SUBSCRIPTIONS",
  NOTIFICATION: "NOTIFICATION"
}

const getAuth = state => state.auth
const getSubscribedCoins = state => state.coins.coins

/**
 * Event channel which allows the saga to react to incoming
 * messages on the socket, or socket open/close in between
 * retries
 */
function socketMessageChannel(socket) {
  return eventChannel(emit => {
    socket.onmessage = evt => {
      try {
        emit(JSON.parse(evt.data))
      } catch (e) {
        console.log("Invalid message from server", evt.data)
      }
    }
    socket.onopen = () => emit(channelMessages.OPEN)
    socket.onclose = () => emit(channelMessages.CLOSE)
    return () => socket.close(undefined, "Shutdown", { keepClosed: true })
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
  } else if (message && message.nature === serverMessages.OPEN_ORDERS) {
    yield put(errorActions.clearBackground("ws"))
    yield put(coinActions.setOrders(message.data.openOrders))
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

function* socketManager() {
  while (true) {
    const auth = yield select(getAuth)
    if (!auth.token || !auth.whitelisted || !auth.loggedIn) {
      console.log("Saga waiting for connect request...")
      yield take(types.CONNECT)
    }

    console.log("Connecting to socket...")
    const token = (yield select(getAuth)).token
    const socket = yield call(ws, "ws", token)
    const socketChannel = yield call(socketMessageChannel, socket)

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
      } else if (action.type === types.RESUBSCRIBE) {
        var coins = yield select(getSubscribedCoins)
        const selectedCoin = yield select(getSelectedCoin)
        if (selectedCoin)
          coins = coins.concat([selectedCoin])
        console.log("Subscribing to tickers", coins)
        yield socket.send(JSON.stringify({
          command: serverMessages.CHANGE_TICKERS,
          tickers: coins.map(coin => webCoinToServerCoin(coin))
        }))
        yield socket.send(JSON.stringify({
          command: serverMessages.CHANGE_OPEN_ORDERS,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        }))
        yield socket.send(JSON.stringify({ command: serverMessages.UPDATE_SUBSCRIPTIONS }))
      } else if (action.type === routerActionTypes.LOCATION_CHANGED) {
        const selectedCoin = yield locationToCoin(action.location)
        yield socket.send(JSON.stringify({
          command: serverMessages.CHANGE_OPEN_ORDERS,
          tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
        }))
        yield socket.send(JSON.stringify({ command: serverMessages.UPDATE_SUBSCRIPTIONS }))
      }
    }
  }
}



/**
 * The saga. Connects a reconnecting websocket and starts the listeners
 * for messages on the channel and outoing messages from redux dispatch.
 */
export function* watcher() {
  while (true) {
    yield race({
      task: all([call(socketManager)])
    })
    console.log("Started listeners")
  }
}