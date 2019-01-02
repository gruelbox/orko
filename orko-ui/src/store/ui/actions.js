import * as types from "./actionTypes"
import Immutable from "seamless-immutable"

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

export function togglePanelVisible(key) {
  return { type: types.TOGGLE_PANEL_VISIBLE, payload: key }
}

export function togglePanelAttached(key) {
  return { type: types.TOGGLE_PANEL_ATTACHED, payload: key }
}

export function resetPanels() {
  return { type: types.RESET_PANELS }
}

export function movePanel(key, position) {
  return { type: types.MOVE_PANEL, payload: { key, position } }
}

export function resizePanel(key, dimensions) {
  return { type: types.RESIZE_PANEL, payload: { key, dimensions } }
}

export function resetLayouts() {
  return { type: types.RESET_ALL_LAYOUTS }
}

export function updateLayouts(layouts) {
  // The filter for size 1 is to workaround a a bug in react-grid-layout
  // https://github.com/STRML/react-grid-layout/issues/870
  return {
    type: types.UPDATE_LAYOUTS,
    payload: Immutable({
      lg: layouts.lg
        .filter(l => l.w !== 1)
        .reduce((acc, val) => {
          acc[val.i] = val
          return acc
        }, {}),
      md: layouts.md
        .filter(l => l.w !== 1)
        .reduce((acc, val) => {
          acc[val.i] = val
          return acc
        }, {}),
      sm: layouts.sm
        .filter(l => l.w !== 1)
        .reduce((acc, val) => {
          acc[val.i] = val
          return acc
        }, {})
    })
  }
}
