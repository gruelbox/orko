import * as types from './actionTypes';
import exchangesService from '../../services/exchanges';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';

export function setOrders(orders) {
  return { type: types.SET_ORDERS, orders }
}

export function setOrderBook(orderBook) {
  return { type: types.SET_ORDERBOOK, orderBook }
}

export function setTradeHistory(tradeHistory) {
  return { type: types.SET_TRADE_HISTORY, tradeHistory }
}

export function setBalance(exchange, currency, balance) {
  return { type: types.SET_BALANCE, currency, balance }
}

export function clearBalances() {
  return { type: types.CLEAR_BALANCES }
}

export function cancelOrder(coin, orderId, orderType) {
  return authActions.wrappedRequest(
    auth => exchangesService.cancelOrder(coin, orderId, orderType, auth.token),
    null,
    error => errorActions.setForeground("Could not cancel order: " + error.message),
    () => ({ type: types.CANCEL_ORDER, orderId })
  );
}