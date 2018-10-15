import * as types from './actionTypes';

export function openAlerts(coin) {
  return { type: types.OPEN_ALERTS, payload: coin }
}

export function closeAlerts() {
  return { type: types.CLOSE_ALERTS }
}

export function openReferencePrice(coin) {
  return { type: types.OPEN_REFERENCE_PRICE, payload: coin }
}

export function closeReferencePrice() {
  return { type: types.CLOSE_REFERENCE_PRICE }
}