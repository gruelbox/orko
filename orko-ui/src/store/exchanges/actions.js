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
    () => exchangesService.fetchExchanges(),
    exchanges => ({ type: types.SET_EXCHANGES, payload: exchanges }),
    error =>
      errorActions.setForeground("Could not fetch exchanges: " + error.message)
  )
}

export function fetchPairs(exchange) {
  return authActions.wrappedRequest(
    () => exchangesService.fetchPairs(exchange),
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

export function submitLimitOrder(exchange, order) {
  return authActions.wrappedRequest(
    () => exchangesService.submitOrder(exchange, order),
    response =>
      coinActions.orderUpdated(
        {
          ...response,
          status: "PENDING_NEW"
        },
        0 // Deliberately old timestamp
      ),
    error =>
      errorActions.setForeground("Could not submit order: " + error.message)
  )
}

export function submitStopOrder(exchange, order) {
  return authActions.wrappedRequest(
    () => exchangesService.submitOrder(exchange, order),
    response =>
      coinActions.orderUpdated(
        {
          ...response,
          status: "PENDING_NEW"
        },
        0 // Deliberately old timestamp
      ),
    error =>
      errorActions.setForeground("Could not submit order: " + error.message)
  )
}

export function cancelOrder(coin, orderId) {
  return async (dispatch, getState) => {
    dispatch(
      coinActions.orderUpdated(
        {
          id: orderId,
          status: "PENDING_CANCEL"
        },
        // Deliberately new enough to be relevant now but get immediately overwritten
        getState().coin.orders.find(o => o.id === orderId).serverTimestamp + 1
      )
    )
    dispatch(
      authActions.wrappedRequest(
        () => exchangesService.cancelOrder(coin, orderId),
        null,
        error =>
          errorActions.setForeground("Could not cancel order: " + error.message)
      )
    )
  }
}
