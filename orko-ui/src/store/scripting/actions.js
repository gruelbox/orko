/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
