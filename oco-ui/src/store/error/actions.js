import * as types from './actionTypes';

export function clearForeground(job) {
  return { type: types.CLEAR_FOREGROUND };
}

export function clearBackground(job) {
  return { type: types.CLEAR_BACKGROUND};
}

export function setForeground(error) {
  return { type: types.SET_FOREGROUND, error };
}

export function setBackground(error) {
  return { type: types.SET_BACKGROUND, error };
}