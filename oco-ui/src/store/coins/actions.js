import * as types from './actionTypes';
import * as tickerActions from '../ticker/actions';

export function add(coin) {
  return (dispatch, getState) => {
    dispatch({ type: types.ADD, coin })
    dispatch(tickerActions.resubscribe())
  }
}

export function remove(coin) {
  return (dispatch, getState) => {
    dispatch({ type: types.REMOVE, coin })
    dispatch(tickerActions.resubscribe())
  }
}