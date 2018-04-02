import Immutable from 'seamless-immutable';
//import * as types from './actionTypes';

const initialState = Immutable({
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    default:
      return state;
  }
}