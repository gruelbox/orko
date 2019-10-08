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
import supportService from "../../services/support"
import { AuthApi } from "modules/auth"

export function fetchMetadata(auth: AuthApi) {
  return auth.wrappedRequest(
    () => supportService.fetchMetadata(),
    json => ({ type: types.SET_META, payload: json }),
    error => errorActions.setForeground("Could not fetch application metadata: " + error.message)
  )
}

export function fetchReleases(auth: AuthApi) {
  return auth.wrappedRequest(
    () => supportService.fetchReleases(),
    json => ({ type: types.SET_RELEASES, payload: json }),
    error => () => console.log("Could not fetch releases", error)
  )
}

export function ignoreVersion() {
  return { type: types.SET_IGNORED_VERSION }
}

export function hideReleases() {
  return { type: types.HIDE_RELEASES }
}
