import * as types from './actionTypes';

export function add(coin) {
  return { type: types.ADD, coin };
}

export function remove(coin) {
  return { type: types.REMOVE, coin };
}