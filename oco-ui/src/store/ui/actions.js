import * as types from './actionTypes';

export function openAlerts(coin) {
  return { type: types.OPEN_ALERTS, payload: coin }
}

export function closeAlerts() {
  return { type: types.CLOSE_ALERTS }
}