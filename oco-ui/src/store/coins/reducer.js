import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const LOCAL_STORAGE_KEY = 'CoinContainer.state';
const loaded = localStorage.getItem(LOCAL_STORAGE_KEY);

const initialState = loaded
  ? Immutable(JSON.parse(loaded).coins)
  : [];

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.ADD:
      var newState = Immutable.concat(state, [action.coin]);
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newState));
      return newState;
    case types.REMOVE:
      var newState = Immutable.filter(state, c => c.key !== action.coin.key);
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newState));
      return newState;
    default:
      return state;
  }
}