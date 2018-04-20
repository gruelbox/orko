import * as types from './actionTypes';
import { take, call, put, race, all, select } from 'redux-saga/effects'
import { ws } from "../../services/fetchUtil"
import { eventChannel } from 'redux-saga'
import { coin as createCoin } from '../coin/reducer'
import { coinFromKey } from '../coin/reducer'
import * as errorActions from '../error/actions'

const channelMessages = {
  OPEN: 'OPEN',
  CLOSE: 'CLOSE',
}

const serverMessages = {
  TICKER: 'TICKER',
  ERROR: 'ERROR',
  INVALID_AUTH: 'INVALID_AUTH',
  START_TICKER: 'START_TICKER',
  STOP_TICKER:'STOP_TICKER',
}

/** 
 * Attempts to reset the subscriptions after an event such as
 * authentication failure.
 */
export function resubscribe() {
  return { type: types.RESUBSCRIBE }
}

/** 
 * Subscribes to the specified ticker.
 */
export function startTicker(coin) {
  return { type: types.START_TICKER, coin }
}


/** 
 * Unsubscribes to the specified ticker.
 */
export function stopTicker(coin) {
  return { type: types.STOP_TICKER, coin }
}

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
    return socket.close
  })
}

function* socketMessageListener(socketChannel) {
  while (true) {
    try {
      const message = yield take(socketChannel);
      if (message === channelMessages.OPEN) {

        // Socket (re)opened so resubscribe to all the tickers we were listening to,
        // and mark the socket open
        yield put({ type: types.RESUBSCRIBE })
        yield put({ type: types.SET_CONNECTION_STATE, connected: true })

      } else if (message === channelMessages.CLOSE) {

        // Mark the socket closed
        yield put({ type: types.SET_CONNECTION_STATE, connected: false })

      } else if (message.nature === serverMessages.INVALID_AUTH) {

        // Error
        yield put(errorActions.addBackground("Authentication failing on socket", "ws"))

      } else if (message.nature === serverMessages.ERROR) {

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
 * Listens for resubscribe requests and forwards the resubscriptions
 */
function* resubscribeListener() {
  while (true) {
    yield take(types.RESUBSCRIBE)

    const state = yield select();
    const keys = Object.keys(state.ticker.coins);
    for (var i = 0 ; i < keys.length ; i++) {
      var coinKey = keys[i]
      yield put({ type: types.START_TICKER, coin: coinFromKey(coinKey) })
    }
  }
}

/**
 * Listens for ticker start requests and forwards them to the socket.
 */
function* tickerStartListener(socket) {
  while (true) {
    const { coin } = yield take(types.START_TICKER)
    const state = yield select();
    socket.send(JSON.stringify({
      command: serverMessages.START_TICKER,
      accessToken: state.auth.token,
      correlationId: "START/" + coin.key,
      ticker: {
        exchange: coin.exchange,
        counter: coin.counter,
        base: coin.base
      }
    }));
  }
}

/**
 * Listens for ticker stop requests and forwards them to the socket.
 */
function* tickerStopListener(socket) {
  while (true) {
    const { coin } = yield take(types.STOP_TICKER);
    const state = yield select();
    socket.send(JSON.stringify({
      command: serverMessages.STOP_TICKER,
      accessToken: state.auth.token,
      correlationId: "STOP/" + coin.key,
      ticker: {
        exchange: coin.exchange,
        counter: coin.counter,
        base: coin.base
      }
    }));
  }
}

/**
 * The saga. Connects a reconnecting websocket and starts the listeners
 * for messages on the channel and outoing messages from redux dispatch.
 */
export function* watcher() {
  while (true) {
    const socket = yield call(ws, "ws")
    const socketChannel = yield call(socketMessageChannel, socket)
    yield put({ type: types.SET_CONNECTION_STATE, connected: socket.readyState === 1 })
    yield race({
      task: all([
        call(socketMessageListener, socketChannel),
        call(tickerStartListener, socket),
        call(tickerStopListener, socket),
        call(resubscribeListener)
      ])
    })
  }
}