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
import Immutable from "seamless-immutable"
import * as types from "./actionTypes"
import { replaceInArray } from "modules/common/util/objectUtils"

export const newScript = Immutable({
  name: "New script",
  parameters: [],
  script: `function start() {
  return SUCCESS
}`
})

export const newParameter = Immutable({
  name: "",
  description: "",
  default: "",
  mandatory: false
})

const initialState = Immutable({
  scripts: [],
  loaded: false
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_SCRIPTS:
      return Immutable.merge(state, {
        scripts: action.payload,
        loaded: true
      })
    case types.DELETE_SCRIPT:
      return Immutable.merge(state, {
        scripts: state.scripts.filter(script => script.id !== action.payload)
      })
    case types.ADD_SCRIPT:
      return Immutable.merge(state, {
        scripts: state.scripts.concat([action.payload])
      })
    case types.UPDATE_SCRIPT:
      return Immutable.merge(state, {
        scripts: replaceInArray(
          state.scripts,
          action.payload,
          s => s.id === action.payload.id
        )
      })
    default:
      return state
  }
}
