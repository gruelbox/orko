import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
  coin: coin("binance", "BTC", "VEN"),
  balance: Immutable({
    available: 1000,
    total: 2000
  }),
  ticker: {
    bid: 1,
    ask: 2,
    last: 3,
    high: 4,
    low: 5,
    open: 6
  }
});

export const shape = {
  counter: PropTypes.string.isRequired,
  base: PropTypes.string.isRequired,
  exchange: PropTypes.string.isRequired,
  key: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  shortName: PropTypes.string.isRequired
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
    case types.SET_COIN:
      console.log(action);
      return Immutable.merge(state, { coin: action.payload });
    default:
      return state;
  }
}