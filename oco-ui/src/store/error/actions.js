import * as types from './actionTypes';

export function clearForeground() {
  return { type: types.CLEAR_FOREGROUND };
}

export function setForeground(error) {
  return { type: types.SET_FOREGROUND, payload: error };
}