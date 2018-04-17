import * as types from './actionTypes';
import { take, call, put, race, all, select } from 'redux-saga/effects'
import { ws } from "../../services/fetchUtil"
import { eventChannel } from 'redux-saga'
import { coin as createCoin } from '../coin/reducer'
import { coinFromKey } from '../coin/reducer'

export function startTicker(coin) {
  return { type: types.START_TICKER, coin }
}

export function stopTicker(coin) {
  return { type: types.STOP_TICKER, coin }
}

function tickerChannel(socket) {
  return eventChannel(emit => {
    socket.onmessage = evt => {
      try {
        emit(JSON.parse(evt.data))
      } catch (e) {
        console.log("Invalid ticker data", evt.data)
      }
    }
    socket.onopen = () => emit("OPEN")
    socket.onclose = () => emit("CLOSE")
    return socket.close
  })
}

function* tickerListener(socketChannel) {
  while (true) {
    const message = yield take(socketChannel);
    if (message === "OPEN") {
      const state = yield select();
      const keys = Object.keys(state.ticker.coins);
      for (var i = 0 ; i < keys.length ; i++) {
        var coinKey = keys[i]
        yield put({ type: types.START_TICKER, coin: coinFromKey(coinKey) })
      }
      yield put({ type: types.SET_CONNECTION_STATE, connected: true })
    } else if (message === "CLOSE") {
      yield put({ type: types.SET_CONNECTION_STATE, connected: false })
    } else {
      yield put({
        type: types.SET_TICKER,
        coin: createCoin(message.spec.exchange, message.spec.counter, message.spec.base),
        ticker: message.ticker
      })
    }
  }
}

function* startListener(socket) {
  while (true) {
    const { coin } = yield take(types.START_TICKER)
    socket.send(JSON.stringify({
      command: "START",
      ticker: {
        exchange: coin.exchange,
        counter: coin.counter,
        base: coin.base
      }
    }));
  }
}

function* stopListener(socket) {
  while (true) {
    const { coin } = yield take(types.STOP_TICKER);
    socket.send(JSON.stringify({
      command: "STOP",
      ticker: {
        exchange: coin.exchange,
        counter: coin.counter,
        base: coin.base
      }
    }));
  }
}

export function* watcher() {
  while (true) {
    const socket = yield call(ws, "ticker-ws")
    const socketChannel = yield call(tickerChannel, socket)
    yield put({ type: types.SET_CONNECTION_STATE, connected: socket.readyState === 1 })
    yield race({
      task: all([
        call(tickerListener, socketChannel),
        call(startListener, socket),
        call(stopListener, socket)
      ])
    })
  }
}