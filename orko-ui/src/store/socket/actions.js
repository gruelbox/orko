import * as types from "./actionTypes"

export function setConnectionState(connected) {
  return { type: types.SET_CONNECTION_STATE, payload: connected }
}