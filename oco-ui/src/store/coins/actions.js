import * as types from './actionTypes';
import * as tickerTypes from '../ticker/actionTypes';

export function add(coin) {
  return async (dispatch, getState) => {
    dispatch({ type: types.ADD, coin })
    dispatch({ type: tickerTypes.RESUBSCRIBE })
  }
}

export function remove(coin) {
  return async (dispatch, getState) => {
    dispatch({ type: types.REMOVE, coin })
    dispatch({ type: tickerTypes.RESUBSCRIBE })
  }
}