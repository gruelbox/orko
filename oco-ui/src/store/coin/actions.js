import * as types from './actionTypes';

export function setCoin(coin) {
  return {
    type: types.SET_COIN,
    payload: coin
  };
}

export function setBalance(balance) {
  return {
    type: types.SET_BALANCE,
    payload: balance
  };
}

export function setTicker(ticker) {
  return {
    type: types.SET_TICKER,
    payload: ticker
  };
}