import * as types from './actionTypes';

export function openAlerts(coin) {
  return { type: types.OPEN_ALERTS, payload: coin }
}

export function closeAlerts() {
  return { type: types.CLOSE_ALERTS }
}

export function openScripts() {
  return { type: types.OPEN_SCRIPTS }
}

export function closeScripts() {
  return { type: types.CLOSE_SCRIPTS }
}

export function openReferencePrice(coin) {
  return { type: types.OPEN_REFERENCE_PRICE, payload: coin }
}

export function closeReferencePrice() {
  return { type: types.CLOSE_REFERENCE_PRICE }
}