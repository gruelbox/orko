import * as types from "./actionTypes"

export function setConnectionState(connected) {
  return { type: types.SET_CONNECTION_STATE, connected }
}

export function setTicker(coin, ticker) {
  return { type: types.SET_TICKER, coin, ticker }
}