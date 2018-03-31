import * as types from './actionTypes';
import * as authActions from '../auth/actions';
import { augmentCoin }  from '../coin/reducer';
import exchangesService from '../../services/exchanges';

export function fetchExchanges() {
  return async(dispatch, getState) => {
    try {
      const response = await exchangesService.fetchExchanges(getState().auth.token);
      if (!response.ok) {
        const authAction = authActions.handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      }
      const exchanges = await response.json();
      dispatch({ type: types.SET_EXCHANGES, exchanges });
    } catch (error) {
      dispatch({ type: types.SET_EXCHANGES_FAILED, error: error.message });
    }
  };
}

export function fetchPairs(exchange) {
  return async(dispatch, getState) => {
    try {
      const response = await exchangesService.fetchPairs(exchange, getState().auth.token);
      if (!response.ok) {
        const authAction = authActions.handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      }
      const pairs = (await response.json()).map(p => augmentCoin(p, exchange));

      dispatch({ type: types.SET_PAIRS, pairs });
    } catch (error) {
      dispatch({ type: types.SET_PAIRS_FAILED, error: error.message });
    }
  };
}