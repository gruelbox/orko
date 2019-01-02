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
import { augmentCoin } from "../../util/coinUtils"
import exchangesService from "../../services/exchanges"
import * as errorActions from "../error/actions"
import * as coinActions from "../coin/actions"

export function fetchExchanges() {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchExchanges(),
    exchanges => ({ type: types.SET_EXCHANGES, payload: exchanges }),
    error =>
      errorActions.setForeground("Could not fetch exchanges: " + error.message)
  )
}

export function fetchPairs(exchange) {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchPairs(exchange),
    json => ({
      type: types.SET_PAIRS,
      payload: json.map(p => augmentCoin(p, exchange))
    }),
    error =>
      errorActions.setForeground(
        "Could not fetch currency pairs for " + exchange + ": " + error.message
      )
  )
}

export function submitOrder(exchange, order) {
  return authActions.wrappedRequest(
    auth => exchangesService.submitOrder(exchange, order),
    response =>
      coinActions.addOrder({
        currencyPair: {
          base: order.base,
          counter: order.counter
        },
        originalAmount: order.amount,
        id: response.id,
        status: "PENDING_NEW",
        type: order.type,
        limitPrice: order.limitPrice,
        cumulativeAmount: 0
      }),
    error =>
      errorActions.setForeground("Could not submit order: " + error.message)
  )
}
