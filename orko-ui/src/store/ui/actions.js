import * as types from "./actionTypes"

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

export function changePanels(panels) {
  return { type: types.CHANGE_PANELS, payload: panels }
}

export function resetPanels() {
  return { type: types.RESET_PANELS }
}
