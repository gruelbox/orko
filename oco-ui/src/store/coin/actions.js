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

export function setOrders(orders) {
  return { type: types.SET_ORDERS, orders }
}

export function cancelOrder(coin, orderId, orderType) {
  return authActions.wrappedRequest(
    auth => exchangesService.cancelOrder(coin, orderId, orderType, auth.token),
    null,
    error => errorActions.setForeground("Could not cancel order: " + error.message),
    () => ({ type: types.CANCEL_ORDER, orderId })
  );
}