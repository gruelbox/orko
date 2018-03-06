import { combineReducers } from 'redux';

import * as balance from './balance';
import * as ticker from './ticker';
import * as auth from './auth';

export const rootReducer = combineReducers({
  balances: balance.reducer,
  tickers: ticker.reducer,
  auth: auth.reducer
})

export const initialState = {
  balances: balance.initialState,
  tickers: ticker.initialState,
  auth: auth.initialState
}

export default rootReducer