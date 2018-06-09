import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
  coins: Immutable({})
});

export const tickerShape = {
  bid: PropTypes.number.isRequired,
  ask: PropTypes.number.isRequired,
  last: PropTypes.number.isRequired,
  high: PropTypes.number.isRequired,
  low: PropTypes.number.isRequired,
  open: PropTypes.number.isRequired
};

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_TICKER:
      return Immutable.merge(state, {
        coins: {
          [action.coin.key]: action.ticker
        }
      }, {deep: true});
    default:
      return state;
  }
}