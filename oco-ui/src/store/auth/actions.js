import * as types from "./actionTypes"
import authService from "../../services/auth"
import * as notificationActions from "../notifications/actions"
import * as coinActions from "../coins/actions"

export function checkWhiteList() {
  return async (dispatch, getState, socket) => {
    try {
      dispatch(notificationActions.trace("Checking whitelist"))
      const result = await authService.checkWhiteList()
      dispatch({ type: types.WHITELIST_UPDATE, payload: Boolean(result) })
      if (result) {
        dispatch(notificationActions.trace("Verified whitelist"))
        dispatch(fetchOktaConfig())
      } else {
        dispatch(notificationActions.trace("Whitelist rejected, disconnecting"))
        socket.disconnect()
      }
    } catch (error) {
      dispatch(notificationActions.trace("Error checking whitelist"))
      dispatch({ type: types.WHITELIST_UPDATE, error: true, payload: error })
    }
  }
}

export function connect() {
  return async (dispatch, getState, socket) => {
    var { config, token } = getState().auth
    if (config && config.clientId) {
      if (token) {
        dispatch(notificationActions.trace("Connecting using token"))
      } else {
        dispatch(notificationActions.trace("Not attempting connect, require token"))
        return
      }
    } else {
      dispatch(notificationActions.trace("Connecting - no authentication configured"))
    }
    dispatch(coinActions.fetch())
    socket.connect()
  }
}

export function whitelist(token) {
  return async (dispatch, getState, socket) => {
    try {
      dispatch(notificationActions.trace("Attempting whitelist"))
      await authService.whitelist(token)
      dispatch(notificationActions.trace("Accepted whitelist"))
      dispatch({ type: types.WHITELIST_UPDATE, payload: true })
      dispatch(fetchOktaConfig())
    } catch (error) {
      dispatch(notificationActions.trace("Error attempting whitelist"))
      dispatch({ type: types.WHITELIST_UPDATE, error: true, payload: error })
    }
  }
}

export function clearWhitelist() {
  return async (dispatch, getState, socket) => {
    try {
      await authService.clearWhiteList()
      dispatch({ type: types.WHITELIST_UPDATE, payload: false })
      socket.disconnect()
    } catch (error) {
      dispatch({ type: types.WHITELIST_UPDATE, error: true, payload: error })
    }
  }
}

export function fetchOktaConfig() {
  return wrappedRequest(
    () => authService.config(),
    config => ({ type: types.SET_OKTA_CONFIG, payload: config }),
    error => notificationActions.localError("Could not fetch authentication data: " + error.message),
    () => connect()
  )
}

export function logout() {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.LOGOUT })
    socket.disconnect()
  }
}

export function setToken(token, userName) {
  return async (dispatch, getState, socket) => {
    dispatch(notificationActions.trace("Setting token for" + userName))
    dispatch({
      type: types.SET_TOKEN,
      payload: {
        token,
        userName
      }
    })
    dispatch(connect())
  }
}

export function invalidateLogin() {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.INVALIDATE_LOGIN })
    socket.disconnect()
  }
}

export function handleHttpResponse(response) {
  if (response.status === 403) {
    return { type: types.WHITELIST_UPDATE, error: true, payload: new Error("Whitelisting expired") }
  } else if (response.status === 401) {
    return invalidateLogin()
  }
  return null
}

export function wrappedRequest(
  apiRequest,
  jsonHandler,
  errorHandler,
  onSuccess
) {
  return async (dispatch, getState) => {
    dispatchWrappedRequest(
      getState().auth,
      dispatch,
      apiRequest,
      jsonHandler,
      errorHandler,
      onSuccess
    )
  }
}

export async function dispatchWrappedRequest(
  auth,
  dispatch,
  apiRequest,
  jsonHandler,
  errorHandler,
  onSuccess
) {
  try {
    // Don't dispatch API requests if we're not authenticated.
    if (!auth.whitelisted || !auth.loggedIn) {
      return
    }

    // Dispatch the request
    const response = await apiRequest(auth)

    if (!response.ok) {
      // Check if the response is an authentication error
      const authAction = handleHttpResponse(response)
      if (authAction !== null) {
        // If so, handle it accordingly
        dispatch(authAction)
      } else {
        // Otherwise, it's an unexpected error
        throw new Error(
          response.statusText
            ? response.statusText
            : "Server error (" + response.status + ")"
        )
      }
    } else {
      if (jsonHandler) dispatch(jsonHandler(await response.json()))
      if (onSuccess) dispatch(onSuccess())
    }
  } catch (error) {
    if (errorHandler) dispatch(errorHandler(error))
  }
}