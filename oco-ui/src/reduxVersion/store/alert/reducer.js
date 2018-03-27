import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
  job: {
    highPrice: '',
    lowPrice: '',
    message: 'Alert'
  }
});

export const shape = {
  highPrice: PropTypes.string,
  lowPrice: PropTypes.string,
  message: PropTypes.string
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