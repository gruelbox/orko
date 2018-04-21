import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const LOCAL_STORAGE_KEY = 'auth';

const loaded = localStorage.getItem(LOCAL_STORAGE_KEY);
const data = loaded ? JSON.parse(loaded) : null;

const initialState = Immutable({
  whitelisted: false,
  loggedIn: !!data,
  token: data ? data.token : null,
  userName: data ? data.userName : null,
  config: null,
  loading: true
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_WHITELIST_STATUS:
      console.debug(action.type, action);
      return Immutable.merge(state, {
        whitelisted: action.status,
        error: null,
        loading: false,
        loggedIn: true // Slightly tricksy. Triggers a round of API calls
                       // which will invalidate the login if it's not working.
      });
    case types.SET_WHITELIST_ERROR:
      console.debug(action.type, action);
      return Immutable.merge(state, {
        whitelisted: false,
        error: action.error,
        loading: false
      });
    case types.SET_WHITELIST_EXPIRED:
      console.log(action.type, action);
      return Immutable.merge(state, {
        whitelisted: false,
        loggedIn: false,
        error: "Whitelisting expired",
        loading: false
      });
    case types.SET_OKTA_CONFIG:
      console.debug(action.type, action);
      return Immutable.merge(state, {
        config: action.config,
      });
    case types.SET_TOKEN:
      console.debug(action.type);
      localStorage.setItem(
        LOCAL_STORAGE_KEY,
        JSON.stringify({
          token: action.token,
          userName: action.userName,
        })
      );
      return Immutable.merge(state, {
        loggedIn: true,
        error: null,
        token: action.token,
        userName: action.userName,
        loading: false
      });
    case types.INVALIDATE_LOGIN:
      console.log(action.type);
      localStorage.removeItem(LOCAL_STORAGE_KEY);
      return Immutable.merge(state, {
        loggedIn: false,
        error: "Not logged in or login expired",
        token: null,
        userName: null,
        loading: false
      });
    case types.LOGOUT:
      console.debug(action.type);
      localStorage.removeItem(LOCAL_STORAGE_KEY);
      return Immutable.merge(state, {
        loggedIn: false,
        error: null,
        token: null,
        userName: null,
        loading: false
      });
    default:
      return state;
  }
}