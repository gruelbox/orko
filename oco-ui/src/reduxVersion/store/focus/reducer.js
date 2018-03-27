import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  updateAction: function() {
    return {
      type: "NULL"
    }
  },
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_UPDATE_ACTION:
      console.log(action);
      return Immutable.merge(state, { updateAction: action.payload });
    default:
      return state;
  }
}