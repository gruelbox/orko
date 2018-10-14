import * as types from "./actionTypes"
import * as authActions from "../auth/actions"
import { augmentCoin } from "../../util/coinUtils"
import exchangesService from "../../services/exchanges"
import * as errorActions from "../error/actions"
import * as coinActions from "../coin/actions"

export function fetchExchanges() {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchExchanges(auth.token),
    exchanges => ({ type: types.SET_EXCHANGES, payload: exchanges }),
    error =>
      errorActions.setForeground("Could not fetch exchanges: " + error.message)
  )
}

export function fetchPairs(exchange) {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchPairs(exchange, auth.token),
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
    auth => exchangesService.submitOrder(auth.token, exchange, order),
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
