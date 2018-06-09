import * as types from './actionTypes';

export function add(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.ADD, coin })
    socket.resubscribe()
  }
}

export function remove(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.REMOVE, coin })
    socket.resubscribe()
  }
}