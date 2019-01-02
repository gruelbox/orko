import * as types from "./actionTypes"
import exchangesService from "../../services/exchanges"
import * as authActions from "../auth/actions"
import * as errorActions from "../error/actions"

export function setOrders(orders) {
  return { type: types.SET_ORDERS, payload: orders }
}

export function addOrder(order) {
  return { type: types.ADD_ORDER, payload: order }
}

export function setOrderBook(orderBook) {
  return { type: types.SET_ORDERBOOK, payload: orderBook }
}

export function setUserTrades(trades) {
  return { type: types.SET_USER_TRADES, payload: trades }
}

export function addUserTrade(trade) {
  return { type: types.ADD_USER_TRADE, payload: trade }
}

export function setBalance(exchange, currency, balance) {
  return { type: types.SET_BALANCE, payload: { currency, balance } }
}

export function addTrade(trade) {
  return { type: types.ADD_TRADE, payload: trade }
}

export function clearTrades() {
  return { type: types.CLEAR_TRADES }
}

export function clearBalances() {
  return { type: types.CLEAR_BALANCES }
}

export function cancelOrder(coin, orderId, orderType) {
  return async (dispatch, getState) => {
    dispatch({ type: types.CANCEL_ORDER, payload: orderId })
    dispatch(doCancelOrder(coin, orderId, orderType))
  }
}

function doCancelOrder(coin, orderId, orderType) {
  return authActions.wrappedRequest(
    auth => exchangesService.cancelOrder(coin, orderId, orderType),
    null,
    error =>
      errorActions.setForeground("Could not cancel order: " + error.message)
  )
}
