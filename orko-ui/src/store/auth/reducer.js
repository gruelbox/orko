import Immutable from "seamless-immutable"
import * as types from "./actionTypes"
import { clearXsrfToken } from "../../services/fetchUtil"

const initialState = Immutable({
  whitelisted: false,
  loggedIn: true,
  config: null,
  loading: true
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.WHITELIST_UPDATE:
      return Immutable.merge(state, {
        whitelisted: action.payload === true,
        error: action.error ? action.payload.message : null,
        loading: false
      })
    case types.SET_OKTA_CONFIG:
      return Immutable.merge(state, {
        config: action.payload
      })
    case types.LOGIN:
      return Immutable.merge(state, {
        loggedIn: true,
        error: null,
        loading: false
      })
    case types.INVALIDATE_LOGIN:
      clearXsrfToken()
      return Immutable.merge(state, {
        loggedIn: false,
        error: "Not logged in or login expired",
        loading: false
      })
    case types.LOGOUT:
      clearXsrfToken()
      return Immutable.merge(state, {
        loggedIn: false,
        error: null,
        loading: false
      })
    default:
      return state
  }
}
