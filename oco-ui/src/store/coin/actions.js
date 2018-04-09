import * as types from './actionTypes';
import exchangesService from '../../services/exchanges';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';

export function fetchTicker(coin) {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchTicker(coin, auth.token),
    ticker => ({ type: types.SET_TICKER, ticker }),
    error => errorActions.addBackground("Could not fetch ticker: " + error.message, "ticker"),
    () => errorActions.clearBackground("ticker")
  );
}

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