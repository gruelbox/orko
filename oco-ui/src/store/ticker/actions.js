import * as types from './actionTypes';
import exchangesService from '../../services/exchanges';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';
import { take, call, put, select, race, all } from 'redux-saga/effects'
import { ws } from "../../services/fetchUtil"
import { eventChannel } from 'redux-saga'

export function* watchTicker(action) {
  const auth = select((state) => state.auth)
  yield authActions.dispatchWrappedRequest(
    auth,
    put,
    auth => call(exchangesService, action.coin, auth),
    ticker => ({ type: types.SET_TICKER, ticker }),
    error => errorActions.addBackground("Could not fetch ticker: " + error.message, "ticker"),
    () => errorActions.clearBackground("ticker")
  )
}

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
        const data = JSON.parse(evt.data);
        emit(data.ticker)
      } catch (e) {
        console.log("Invalid ticker data", evt.data)
      }
    }
    return socket.close
  })
}

function* tickerListener(socketChannel) {
  while (true) {
    const ticker = yield take(socketChannel);
    yield put({ type: types.SET_TICKER, ticker })
  }
}

function* startListener(socket) {
  while (true) {
    const { coin } = yield take(types.START_TICKER)
    console.log("-> START/", coin.key)
    socket.send("START/" + coin.key);
  }
}

function* stopListener(socket) {
  while (true) {
    const { coin } = yield take(types.STOP_TICKER);
    console.log("-> STOP", coin.key)
    socket.send("STOP/" + coin.key);
  }
}

export function* watcher() {
  while (true) {
    const socket = yield call(ws, "ticker-ws")
    const socketChannel = yield call(tickerChannel, socket)
    yield race({
      task: all([
        call(tickerListener, socketChannel),
        call(startListener, socket),
        call(stopListener, socket)
      ])
    })
  }
}
