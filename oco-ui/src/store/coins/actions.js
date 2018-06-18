import * as types from './actionTypes'
import * as tickerActions from '../ticker/actions'
import exchangesService from '../../services/exchanges'
import * as authActions from '../auth/actions'
import * as errorActions from '../error/actions'
import * as notificationActions from '../notifications/actions'
import { coinFromTicker, tickerFromCoin } from '../../util/coinUtils'

export function fetch() {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchSubscriptions(auth.token),
    json => ({ type: types.SET, payload: json.map(t => coinFromTicker(t)) }),
    error => errorActions.setForeground("Could not fetch coin list: " + error.message),
    () => notificationActions.localMessage("Fetched coins")
  );
}

export function add(coin) {
  return authActions.wrappedRequest(
    auth => exchangesService.addSubscription(auth.token, JSON.stringify(tickerFromCoin(coin))),
    null,
    error => errorActions.setForeground("Could not add subscription: " + error.message),
    () => applyAdd(coin)
  );
}

function applyAdd(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.ADD, payload: coin })
    socket.resubscribe()
  }
}

export function remove(coin) {
  return authActions.wrappedRequest(
    auth => exchangesService.removeSubscription(auth.token, JSON.stringify(tickerFromCoin(coin))),
    null,
    error => errorActions.setForeground("Could not remove subscription: " + error.message),
    () => applyRemove(coin)
  );
}

function applyRemove(coin) {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.REMOVE, payload: coin })
    socket.resubscribe()
    dispatch(tickerActions.clearTicker(coin))
  }
}