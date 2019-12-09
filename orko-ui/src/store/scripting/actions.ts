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
import * as errorActions from "../error/actions"
import scriptService from "../../services/script"
import { AuthApi } from "modules/auth"

export function fetch(auth: AuthApi) {
  return auth.wrappedRequest(
    () => scriptService.fetchScripts(),
    json => ({ type: types.SET_SCRIPTS, payload: json }),
    error => errorActions.setForeground("Could not fetch scripts: " + error.message)
  )
}

export function remove(auth: AuthApi, id) {
  return auth.wrappedRequest(
    () => scriptService.deleteScript(id),
    null,
    error => errorActions.setForeground("Could not delete script: " + error.message),
    () => ({ type: types.DELETE_SCRIPT, payload: id })
  )
}

export function add(auth: AuthApi, script) {
  return auth.wrappedRequest(
    () => scriptService.saveScript(script),
    null,
    error => errorActions.setForeground("Could not add script: " + error.message),
    () => ({ type: types.ADD_SCRIPT, payload: script })
  )
}

export function update(auth: AuthApi, script) {
  return auth.wrappedRequest(
    () => scriptService.saveScript(script),
    null,
    error => errorActions.setForeground("Could not update script: " + error.message),
    () => ({ type: types.UPDATE_SCRIPT, payload: script })
  )
}
