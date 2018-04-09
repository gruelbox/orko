import * as types from './actionTypes';

export function clearForeground() {
  return { type: types.CLEAR_FOREGROUND };
}

export function clearBackground(key) {
  return { type: types.CLEAR_BACKGROUND, key };
}

export function setForeground(error) {
  return { type: types.SET_FOREGROUND, error };
}

export function addBackground(error, key = types.ERROR_KEY_GLOBAL) {
  return { type: types.ADD_BACKGROUND, error, key };
}