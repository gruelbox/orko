import * as types from "./actionTypes"
import * as authActions from "../auth/actions"
import * as errorActions from "../error/actions"
import scriptService from "../../services/script"

export function fetch() {
  return authActions.wrappedRequest(
    () => scriptService.fetchScripts(),
    json => ({ type: types.SET_SCRIPTS, payload: json }),
    error =>
      errorActions.setForeground("Could not fetch scripts: " + error.message)
  )
}

export function remove(id) {
  return authActions.wrappedRequest(
    () => scriptService.deleteScript(id),
    null,
    error =>
      errorActions.setForeground("Could not delete script: " + error.message),
    () => ({ type: types.DELETE_SCRIPT, payload: id })
  )
}

export function add(script) {
  return authActions.wrappedRequest(
    () => scriptService.saveScript(script),
    null,
    error =>
      errorActions.setForeground("Could not add script: " + error.message),
    () => ({ type: types.ADD_SCRIPT, payload: script })
  )
}

export function update(script) {
  return authActions.wrappedRequest(
    () => scriptService.saveScript(script),
    null,
    error =>
      errorActions.setForeground("Could not update script: " + error.message),
    () => ({ type: types.UPDATE_SCRIPT, payload: script })
  )
}
