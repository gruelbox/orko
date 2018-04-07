import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  errorForeground: null,
  errorBackground: null
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.CLEAR_FOREGROUND:
      console.log(action.type, action);
      return Immutable.merge(state, {
        errorForeground: null,
      });
    case types.CLEAR_BACKGROUND:
      console.log(action.type, action);
      return Immutable.merge(state, {
        errorBackground: null,
      });
    case types.SET_FOREGROUND:
      console.log(action.type, action);
      return Immutable.merge(state, {
        errorForeground: action.error,
      });
    case types.SET_BACKGROUND:
      console.log(action.type, action);
      return Immutable.merge(state, {
        errorBackground: action.error,
      });
    default:
      return state;
  }
}