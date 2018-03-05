import { combineReducers } from 'redux';

import * as balance from './balance';
import * as ticker from './ticker';

export const rootReducer = combineReducers({
  balances: balance.reducer,
  tickers: ticker.reducer,
})

export const initialState = {
  balances: balance.initialState,
  tickers: ticker.initialState,
}

export default rootReducer