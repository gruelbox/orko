import * as types from './actionTypes';
import exchangesService from '../../services/exchanges';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';

export function fetchTicker(coin) {
  return async(dispatch, getState) => {
    try {
      const response = await exchangesService.fetchTicker(coin, getState().auth.token);
      if (!response.ok) {
        const authAction = authActions.handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      }
      const ticker = await response.json();
      dispatch({ type: types.SET_TICKER, ticker });
    } catch (error) {
      dispatch(errorActions.setBackground("Could not fetch ticker: " + error.message));
    }
  };
}

export function fetchBalance(coin) {
  return async(dispatch, getState) => {
    try {
      const response = await exchangesService.fetchBalance(coin, getState().auth.token);
      if (!response.ok) {
        const authAction = authActions.handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      }
      const balance = await response.json();
      dispatch({ type: types.SET_BALANCE, balance });
    } catch (error) {
      // This is fne. It's often not available.
    }
  };
}