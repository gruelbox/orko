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

const initialState = Immutable({
  referencePrices: Immutable({})
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_REFERENCE_PRICE:
      return Immutable.merge(
        state,
        {
          referencePrices: {
            [action.payload.coin.key]: action.payload.price
          }
        },
        { deep: true }
      )
    case types.SET_REFERENCE_PRICES:
      return Immutable.merge(state, { referencePrices: action.payload })
    default:
      return state
  }
}
