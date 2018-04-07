import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  error: null
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.CLEAR_ERROR:
      console.log(action.type, action);
      return Immutable.merge(state, {
        error: null,
      });
    case types.SET_ERROR:
      console.log(action.type, action);
      return Immutable.merge(state, {
        error: action.error,
      });
    default:
      return state;
  }
}