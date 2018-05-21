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
        });
      }
      break;
    case types.CLEAR_BACKGROUND:
      if (state.errorBackground.length > 0) {
        return Immutable.merge(state, {
          errorBackground: state.errorBackground.without(action.key),
        });
      }
      break;
    case types.SET_FOREGROUND:
      return Immutable.merge(state, {
        errorForeground: action.error,
      });
    case types.ADD_BACKGROUND:
      return Immutable.merge(state, {
        errorBackground: {
          [action.key]: action.error
        }
      }, {deep: true});
  }
  return state
}