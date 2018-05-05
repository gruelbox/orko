import * as types from './actionTypes';

export function add(notification) {
  return { type: types.ADD, notification }
}