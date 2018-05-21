import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  exchanges: Immutable([]),
  pairs: Immutable([]),
  error: null,
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_EXCHANGES:
      return Immutable.merge(state, {
        exchanges: Immutable(action.exchanges),
        error: null,
      });
    case types.SET_PAIRS:
      return Immutable.merge(state, {
        pairs: Immutable(action.pairs),
        error: null,
      });
    default:
      return state;
  }
}