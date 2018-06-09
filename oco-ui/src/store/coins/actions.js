import * as types from './actionTypes';
import * as socket from "../socketStoreIntegration"

export function add(coin) {
  return (dispatch, getState) => {
    dispatch({ type: types.ADD, coin })
    socket.resubscribe()
  }
}

export function remove(coin) {
  return (dispatch, getState) => {
    dispatch({ type: types.REMOVE, coin })
    socket.resubscribe()
  }
}