import * as types from './actionTypes';
import { take, call, put, race, all, select } from 'redux-saga/effects'
import { ws } from "../../services/fetchUtil"
import { eventChannel } from 'redux-saga'
import { coin as createCoin } from '../coin/reducer'
import * as errorActions from '../error/actions'

const channelMessages = {
  OPEN: 'OPEN',
  CLOSE: 'CLOSE',
}

const serverMessages = {
  TICKER: 'TICKER',
  ERROR: 'ERROR',
  CHANGE_TICKERS: 'CHANGE_TICKERS',
}

export const getAuthToken = state => state.auth.token
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
    socket.onclose = (e) => emit(channelMessages.CLOSE)
    return socket.close
  })
}

function* socketMessageListener(socketChannel, getState) {
  while (true) {
    try {
      const message = yield take(socketChannel);
      if (message === channelMessages.OPEN) {
        console.log("Socket (re)connected")

        // Socket (re)opened so resubscribe to all the tickers we were listening to,
        // and mark the socket open
        yield put({ type: types.SET_CONNECTION_STATE, connected: true })
        yield put({ type: types.RESUBSCRIBE })

      } else if (message === channelMessages.CLOSE) {
        console.log("Socket connection lost")

        // Mark the socket closed
        yield put({ type: types.SET_CONNECTION_STATE, connected: false })

      } else if (message.nature === serverMessages.ERROR) {
        console.log("Error from socket")

        // Error
        yield put(errorActions.addBackground(message.data, "ws"))

      } else if (message.nature === serverMessages.TICKER) {

        const coin = createCoin(message.data.spec.exchange, message.data.spec.counter, message.data.spec.base)
        const ticker = message.data.ticker

        yield put(errorActions.clearBackground("ws"))
        yield put({ type: types.SET_TICKER, coin, ticker })

      } else {

        yield put(errorActions.addBackground("Unknown message from server: " + JSON.stringify(message), "ws"))

      }
    } catch (ex) {
      yield put(errorActions.addBackground("Unknown error in socker handler: " + ex.message, "ws"))
    }
  }
}

/**
 * Listens for requests to update the list of subscribed tickers
 * and forwards these requests to the socket.
 */
function* resubscribeListener(socket) {
  while (true) {
    yield take(types.RESUBSCRIBE)
    const connected = yield select(getConnected)
    const coins = yield select(getSubscribedCoins)
    if (connected) {
      console.log("Subscribing to tickers", coins)
      socket.send(JSON.stringify({
        command: serverMessages.CHANGE_TICKERS,
        correlationId: "CHANGE",
        tickers: coins.map(coin => ({
          exchange: coin.exchange,
          counter: coin.counter,
          base: coin.base
        }))
      }));
    }
  }
}

function openSocket(getState) {
  console.log("Connecting to socket...")
  return ws("ws", () => getState().auth.token)
}

/**
 * The saga. Connects a reconnecting websocket and starts the listeners
 * for messages on the channel and outoing messages from redux dispatch.
 */
export function* watcher(dispatch, getState) {
  while (true) {
    const socket = yield call(openSocket, getState)
    const socketChannel = yield call(socketMessageChannel, socket)
    yield race({
      task: all([
        call(socketMessageListener, socketChannel, getState),
        call(resubscribeListener, socket)
      ])
    })
    console.log("Started listeners")
  }
}