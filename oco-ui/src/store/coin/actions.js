import * as types from './actionTypes';
import exchangesService from '../../services/exchanges';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';

export function fetchBalance(coin) {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchBalance(coin, auth.token),
    balance => ({ type: types.SET_BALANCE, balance }),
    error => ({ type: types.SET_BALANCE_UNAVAILABLE }),
  );
}

export function fetchOrders(coin) {
  // No error handling. Order books are often missing where there's no API key.
  return authActions.wrappedRequest(
    auth => exchangesService.fetchOrders(coin, auth.token),
    orders => ({ type: types.SET_ORDERS, orders }),
    error => ({ type: types.SET_ORDERS_UNAVAILABLE }),
  );
}

export function setOrders(orders) {
  return { type: types.SET_ORDERS, orders }
}

export function setOrderBook(orderBook) {
  return { type: types.SET_ORDERBOOK, orderBook }
}

export function setTradeHistory(tradeHistory) {
  return { type: types.SET_TRADE_HISTORY, tradeHistory }
}

export function cancelOrder(coin, orderId, orderType) {
  return authActions.wrappedRequest(
    auth => exchangesService.cancelOrder(coin, orderId, orderType, auth.token),
    null,
    error => errorActions.setForeground("Could not cancel order: " + error.message),
    () => ({ type: types.CANCEL_ORDER, orderId })
  );
}