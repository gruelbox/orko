import * as types from "./actionTypes"

export function remove(id) {
  return { type: types.DELETE_SCRIPT, payload: id }
}

export function add(script) {
  return { type: types.ADD_SCRIPT, payload: script }
}

export function update(script) {
  return { type: types.UPDATE_SCRIPT, payload: script }
}
