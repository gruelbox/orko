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
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.WHITELIST_UPDATE:
      return Immutable.merge(state, {
        whitelisted: action.payload === true,
        error: action.error ? action.payload.message : null,
        loading: false,
        loggedIn: !action.error && (action.payload === true) // Slightly tricksy. Triggers a round of API calls
                                                             // which will invalidate the login if it's not working.
      })
    case types.SET_OKTA_CONFIG:
      return Immutable.merge(state, {
        config: action.payload,
      })
    case types.SET_TOKEN:
      localStorage.setItem(
        LOCAL_STORAGE_KEY,
        JSON.stringify({
          token: action.payload.token,
          userName: action.payload.userName,
        })
      )
      return Immutable.merge(state, {
        loggedIn: true,
        error: null,
        token: action.payload.token,
        userName: action.payload.userName,
        loading: false
      })
    case types.INVALIDATE_LOGIN:
      localStorage.removeItem(LOCAL_STORAGE_KEY);
      return Immutable.merge(state, {
        loggedIn: false,
        error: "Not logged in or login expired",
        token: null,
        userName: null,
        loading: false
      })
    case types.LOGOUT:
      localStorage.removeItem(LOCAL_STORAGE_KEY);
      return Immutable.merge(state, {
        loggedIn: false,
        error: null,
        token: null,
        userName: null,
        loading: false
      })
    default:
      return state
  }
}