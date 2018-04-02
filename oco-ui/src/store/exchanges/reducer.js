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
      console.log(action.type, action);
      return Immutable.merge(state, {
        exchanges: Immutable(action.exchanges),
        error: null,
      });
    case types.SET_PAIRS:
      console.log(action.type, action);
      return Immutable.merge(state, {
        pairs: Immutable(action.pairs),
        error: null,
      });
    case types.SET_EXCHANGES_FAILED:
      console.log(action.type, action);
      return Immutable.merge(state, {
        exchanges: Immutable([]),
        error: state.loading ? null : "Error fetching exchanges: " + action.error,
      });
    case types.SET_PAIRS_FAILED:
      console.log(action.type, action);
      return Immutable.merge(state, {
        pairs: Immutable([]),
        error: state.loading ? null : "Error fetching pairs: " + action.error,
      });
    default:
      return state;
  }
}