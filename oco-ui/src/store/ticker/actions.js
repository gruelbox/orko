import * as types from "./actionTypes"

export function setTicker(coin, ticker) {
  return { type: types.SET_TICKER, payload: {coin, ticker} }
}