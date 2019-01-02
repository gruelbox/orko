/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
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
