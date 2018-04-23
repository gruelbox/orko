import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
  coins: Immutable({}),
  connected: false
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
    case types.RESUBSCRIBE:
      console.debug(action.type, action);
      return Immutable.merge(state, { 
        coins: Immutable({})
      });
    case types.SET_TICKER:
      console.debug(action.type, action);
      return Immutable.merge(state, {
        coins: {
          [action.coin.key]: action.ticker
        }
      }, {deep: true});
    case types.SET_CONNECTION_STATE:
      console.debug(action.type, action);
      return Immutable.merge(state, { connected: action.connected });
    default:
      return state;
  }
}