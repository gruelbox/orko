import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

export const BUY = 'BUY';
export const SELL = 'SELL';

const initialState = Immutable({
  job: {
    price: '',
    amount: '',
    direction: BUY
  }
});

export const shape = {
  price: PropTypes.string,
  amount: PropTypes.string,
  direction: PropTypes.string
};

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.UPDATE:
      console.log(action);
      return Immutable.merge(state, { job: action.payload }, {deep: true});
    default:
      return state;
  }
}