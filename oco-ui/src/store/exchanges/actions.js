import * as types from './actionTypes';
import * as authActions from '../auth/actions';
import { augmentCoin }  from '../coin/reducer';
import exchangesService from '../../services/exchanges';
import * as errorActions from '../error/actions';

export function fetchExchanges() {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchExchanges(auth.token),
    exchanges => ({ type: types.SET_EXCHANGES, exchanges }),
    error => errorActions.setBackground("Could not fetch exchanges: " + error.message)
  );
}

export function fetchPairs(exchange) {
  return authActions.wrappedRequest(
    auth => exchangesService.fetchPairs(exchange, auth.token),
    json => ({ type: types.SET_PAIRS, pairs: json.map(p => augmentCoin(p, exchange)) }),
    error => errorActions.setBackground("Could not fetch currency pairs for " + exchange + ": " + error.message)
  );
}