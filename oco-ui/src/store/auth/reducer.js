import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
  whitelisted: false,
  loggedIn: false,
  token: {
    userName: 'bully',
    password: 'boys'
  },
  error: null,
  loading: true
});

export const shape = {
  whitelisted: PropTypes.bool.isRequired,
  loggedIn: PropTypes.bool.isRequired,
  token: PropTypes.shape({
    userName: PropTypes.string.isRequired,
    password: PropTypes.string.isRequired
  }),
  error: PropTypes.string,
  loading: PropTypes.bool
};

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_WHITELIST_STATUS:
      console.log(action);
      return Immutable.merge(state, {
        whitelisted: action.status,
        error: null,
        loading: false
      });
    case types.SET_WHITELIST_ERROR:
      console.log(action);
      return Immutable.merge(state, {
        whitelisted: false,
        error: action.error,
        loading: false
      });
    case types.SET_WHITELIST_EXPIRED:
      console.log(action);
      return Immutable.merge(state, {
        whitelisted: false,
        loggedIn: false,
        error: "Whitelisting expired",
        loading: false
      });
    case types.SET_LOGIN_FAILED:
      console.log(action);
      return Immutable.merge(state, {
        whitelisted: true,
        loggedIn: false,
        error: state.loading ? null : "Invalid username/password",
        loading: false
      });
    case types.SET_LOGGED_OUT:
      console.log(action);
      return Immutable.merge(state, {
        loggedIn: false,
        error: null
      });
    case types.SET_LOGIN_ERROR:
      console.log(action);
      return Immutable.merge(state, {
        whitelisted: true,
        loggedIn: false,
        error: state.loading ? null : action.error,
        loading: false
      });
    case types.SET_LOGIN_SUCCESS:
      console.log(action);
      return Immutable.merge(state, {
        whitelisted: true,
        loggedIn: true,
        error: null,
        token: action.token,
        loading: false
      });
    default:
      return state;
  }
}