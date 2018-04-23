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
import * as errorActions from "../error/actions"

const channelMessages = {
  OPEN: "OPEN",
  CLOSE: "CLOSE"
}

const serverMessages = {
  TICKER: "TICKER",
  ERROR: "ERROR",
  CHANGE_TICKERS: "CHANGE_TICKERS"
}

export const getAuth = state => state.auth
export const getSubscribedCoins = state => state.coins.coins
export const getConnected = state => state.ticker.connected

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
    const coin = createCoin(message.data.spec.exchange, message.data.spec.counter, message.data.spec.base)
    yield put({
      type: types.SET_TICKER,
      coin,
      ticker: message.data.ticker
    })
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
    types.RESUBSCRIBE
  ])
}

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
        const coins = yield select(getSubscribedCoins)
        console.log("Subscribing to tickers", coins)
        yield socket.send(
          JSON.stringify({
            command: serverMessages.CHANGE_TICKERS,
            correlationId: "CHANGE",
            tickers: coins.map(coin => ({
              exchange: coin.exchange,
              counter: coin.counter,
              base: coin.base
            }))
          })
        )
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
