import * as types from './actionTypes';

export function openAlerts(coin) {
  return { type: types.OPEN_ALERTS, coin }
}

export function closeAlerts() {
  return { type: types.CLOSE_ALERTS }
}