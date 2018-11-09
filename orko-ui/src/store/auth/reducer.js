import Immutable from "seamless-immutable"
import * as types from "./actionTypes"
import Cookies from "cookies-js"

const ACCESS_TOKEN = "accessToken"

const initialState = Immutable({
  whitelisted: false,
  loggedIn: !!Cookies.get(ACCESS_TOKEN),
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
      if (action.payload.token) {
        Cookies.set(ACCESS_TOKEN, action.payload.token, {
          path: "/",
          expires: action.payload.expiry,
          secure: window.location.protocol === "https:"
        })
      }
      return Immutable.merge(state, {
        loggedIn: true,
        error: null,
        loading: false
      })
    case types.INVALIDATE_LOGIN:
      Cookies.expire(ACCESS_TOKEN)
      return Immutable.merge(state, {
        loggedIn: false,
        error: "Not logged in or login expired",
        loading: false
      })
    case types.LOGOUT:
      Cookies.expire(ACCESS_TOKEN)
      return Immutable.merge(state, {
        loggedIn: false,
        error: null,
        loading: false
      })
    default:
      return state
  }
}
