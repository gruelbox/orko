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

const initialState = Immutable({
  notifications: Immutable([]),
  statusCallbacks: Immutable([])
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.CLEAR:
      return Immutable.merge(state, { notifications: [] })
    case types.ADD:
      const notification = Immutable.set(
        Immutable(action.payload),
        "dateTime",
        new Date()
      )
      return Immutable.merge(state, {
        notifications: Immutable([notification]).concat(state.notifications)
      })
    case types.COMPLETE_CALLBACK:
      //console.log(action.type, action)
      return Immutable.merge(state, {
        statusCallbacks: Immutable.without(state.statusCallbacks, [
          action.payload
        ])
      })
    case types.REQUEST_CALLBACK:
      //console.log(action.type, action)
      return Immutable.merge(
        state,
        {
          statusCallbacks: {
            [action.payload.requestId]: {
              callback: action.payload.callback
            }
          }
        },
        { deep: true }
      )
    case types.DEFER_CALLBACK:
      //console.log(action.type, action)
      return Immutable.merge(
        state,
        {
          statusCallbacks: {
            [action.payload.requestId]: {
              status: action.payload
            }
          }
        },
        { deep: true }
      )
    default:
      return state
  }
}
