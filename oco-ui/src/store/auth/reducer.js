import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
  whitelisted: false,
  loggedIn: false,
  userName: '',
  password: '',
  error: null,
  loading: true
});

export const shape = {
  whitelisted: PropTypes.bool.isRequired,
  loggedIn: PropTypes.bool.isRequired,
  userName: PropTypes.string.isRequired,
  password: PropTypes.string.isRequired
};

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.UPDATE:
      console.log(action);
      return Immutable.merge(state, {
        ...action.payload,
        loading: false
      });
    default:
      return state;
  }
}