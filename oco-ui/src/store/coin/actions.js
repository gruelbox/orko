import * as types from './actionTypes';
import exchangesService from '../../services/exchanges';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';

export function fetchTicker(coin) {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchTicker(coin, auth.token),
    ticker => ({ type: types.SET_TICKER, ticker }),
    error => errorActions.setBackground("Could not fetch ticker: " + error.message)
  );
}

export function fetchBalance(coin) {
  // No error handling. Balances are often missing where there's no API key.
  return authActions.wrappedRequest(
    auth => exchangesService.fetchBalance(coin, auth.token),
    balance => ({ type: types.SET_BALANCE, balance })
  );
}