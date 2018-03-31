import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
  balance: undefined,
  ticker: undefined
});

export const coinShape = {
  counter: PropTypes.string.isRequired,
  base: PropTypes.string.isRequired,
  exchange: PropTypes.string.isRequired,
  key: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  shortName: PropTypes.string.isRequired
};

export const balanceShape = {
  available: PropTypes.string.isRequired,
  total: PropTypes.string.isRequired
};

export const tickerShape = {
  bid: PropTypes.number.isRequired,
  ask: PropTypes.number.isRequired,
  last: PropTypes.number.isRequired,
  high: PropTypes.number.isRequired,
  low: PropTypes.number.isRequired,
  open: PropTypes.number.isRequired
};

export function coin(exchange, counter, base) {
  return augmentCoin({
    counter: counter,
    base: base
  }, exchange);
}

export function augmentCoin(p, exchange) {
  return Immutable.merge(p, {
    exchange: exchange,
    key: exchange + "/" + p.counter + "/" + p.base,
    name: p.base + "/" + p.counter + " (" + exchange + ")",
    shortName: p.base + "/" + p.counter
  });
}

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_BALANCE:
      console.log(action);
      return Immutable.merge(state, { balance: action.balance });
    case types.SET_BALANCE_FAILED:
      console.log(action);
      return Immutable.merge(state, { balance: undefined });
    case types.SET_TICKER:
      console.log(action);
      return Immutable.merge(state, { ticker: action.ticker });
    case types.SET_TICKER_FAILED:
      console.log(action);
      return Immutable.merge(state, { ticker: undefined });
    default:
      return state;
  }
}