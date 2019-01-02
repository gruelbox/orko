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
import * as tickerActions from "../ticker/actions"
import exchangesService from "../../services/exchanges"
import * as authActions from "../auth/actions"
import * as errorActions from "../error/actions"
import { coinFromTicker, tickerFromCoin } from "../../util/coinUtils"

export function fetch() {
  return authActions.wrappedRequest(
    () => exchangesService.fetchSubscriptions(),
    json => ({ type: types.SET, payload: json.map(t => coinFromTicker(t)) }),
    error =>
      errorActions.setForeground("Could not fetch coin list: " + error.message),
    () => multiFetchMetadata()
  )
}

export function fetchReferencePrices() {
  return authActions.wrappedRequest(
    () => exchangesService.fetchReferencePrices(),
    json => ({ type: types.SET_REFERENCE_PRICES, payload: json }),
    error =>
      errorActions.setForeground("Could not fetch coin list: " + error.message)
  )
}

export function add(coin) {
  return authActions.wrappedRequest(
    () =>
      exchangesService.addSubscription(JSON.stringify(tickerFromCoin(coin))),
    null,
    error =>
      errorActions.setForeground(
        "Could not add subscription: " + error.message
      ),
    () => applyAdd(coin)
  )
}

function multiFetchMetadata() {
  return async (dispatch, getState) => {
    getState().coins.coins.forEach(coin => dispatch(fetchMetadata(coin)))
  }
}

function fetchMetadata(coin) {
  return authActions.wrappedRequest(
    () => exchangesService.fetchMetadata(coin),
    json => ({ type: types.SET_META, payload: { coin: coin, meta: json } }),
    error =>
      errorActions.setForeground(
        "Could not fetch coin metadata for " + coin.name + " : " + error.message
      )
  )
}

function applyAdd(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.ADD, payload: coin })
    dispatch(fetchMetadata(coin))
    socket.resubscribe()
  }
}

export function remove(coin) {
  return authActions.wrappedRequest(
    () =>
      exchangesService.removeSubscription(JSON.stringify(tickerFromCoin(coin))),
    null,
    error =>
      errorActions.setForeground(
        "Could not remove subscription: " + error.message
      ),
    () => applyRemove(coin)
  )
}

function applyRemove(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.REMOVE, payload: coin })
    socket.resubscribe()
    dispatch(tickerActions.clearTicker(coin))
  }
}

export function setReferencePrice(coin, price) {
  return authActions.wrappedRequest(
    () => exchangesService.setReferencePrice(coin, price),
    null,
    error =>
      errorActions.setForeground(
        "Could not set reference price on server: " + error.message
      ),
    () => ({ type: types.SET_REFERENCE_PRICE, payload: { coin, price } })
  )
}
