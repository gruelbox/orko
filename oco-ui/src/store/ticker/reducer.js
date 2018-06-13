import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  coins: Immutable({})
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_TICKER:
      return Immutable.merge(state, {
        coins: {
          [action.payload.coin.key]: action.payload.ticker
        }
      }, {deep: true});
    default:
      return state;
  }
}