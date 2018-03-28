import * as types from './actionTypes';

export function setCoin(coin) {
  return {
    type: types.SET_COIN,
    payload: coin
  };
}