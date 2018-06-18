import * as types from './actionTypes';
import * as tickerActions from '../ticker/actions'

export function add(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.ADD, payload: coin })
    socket.resubscribe()
  }
}

export function remove(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.REMOVE, payload: coin })
    socket.resubscribe()
    dispatch(tickerActions.clearTicker(coin))
  }
}