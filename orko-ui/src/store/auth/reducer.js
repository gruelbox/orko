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
import Immutable from "seamless-immutable"
import * as types from "./actionTypes"
import { clearXsrfToken } from "../../services/fetchUtil"

const initialState = Immutable({
  whitelisted: false,
  loggedIn: true,
  config: null,
  loading: true
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.WHITELIST_UPDATE:
      return Immutable.merge(state, {
        whitelisted: action.payload === true,
        error: action.error ? action.payload.message : null,
        loading: false
      })
    case types.SET_OKTA_CONFIG:
      return Immutable.merge(state, {
        config: action.payload
      })
    case types.LOGIN:
      return Immutable.merge(state, {
        loggedIn: true,
        error: null,
        loading: false
      })
    case types.INVALIDATE_LOGIN:
      clearXsrfToken()
      return Immutable.merge(state, {
        loggedIn: false,
        error: "Not logged in or login expired",
        loading: false
      })
    case types.LOGOUT:
      clearXsrfToken()
      return Immutable.merge(state, {
        loggedIn: false,
        error: null,
        loading: false
      })
    default:
      return state
  }
}
