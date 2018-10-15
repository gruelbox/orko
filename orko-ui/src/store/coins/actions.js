import * as types from "./actionTypes"
import * as tickerActions from "../ticker/actions"
import exchangesService from "../../services/exchanges"
import * as authActions from "../auth/actions"
import * as errorActions from "../error/actions"
import { coinFromTicker, tickerFromCoin } from "../../util/coinUtils"

export function fetch() {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchSubscriptions(auth.token),
    json => ({ type: types.SET, payload: json.map(t => coinFromTicker(t)) }),
    error =>
      errorActions.setForeground("Could not fetch coin list: " + error.message),
    () => multiFetchMetadata()
  )
}

export function fetchReferencePrices() {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchReferencePrices(auth.token),
    json => ({ type: types.SET_REFERENCE_PRICES, payload: json }),
    error =>
      errorActions.setForeground("Could not fetch coin list: " + error.message)
  )
}

export function add(coin) {
  return authActions.wrappedRequest(
    auth =>
      exchangesService.addSubscription(
        auth.token,
        JSON.stringify(tickerFromCoin(coin))
      ),
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
    auth => exchangesService.fetchMetadata(coin, auth.token),
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
    auth =>
      exchangesService.removeSubscription(
        auth.token,
        JSON.stringify(tickerFromCoin(coin))
      ),
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
    auth => exchangesService.setReferencePrice(auth.token, coin, price),
    null,
    error =>
      errorActions.setForeground(
        "Could not set reference price on server: " + error.message
      ),
    () => ({ type: types.SET_REFERENCE_PRICE, payload: { coin, price } })
  )
}
