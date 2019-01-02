import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  errorForeground: null,
  errorBackground: Immutable({})
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.CLEAR_FOREGROUND:
      if (state.errorForeground) {
        return Immutable.merge(state, {
          errorForeground: null,
        })
      }
      break;
    case types.SET_FOREGROUND:
      return Immutable.merge(state, {
        errorForeground: action.payload,
      })
    default:
      break
  }
  return state
}