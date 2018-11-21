import * as types from "./actionTypes"
import authService from "../../services/auth"
import * as notificationActions from "../notifications/actions"
import * as errorActions from "../error/actions"
import * as coinActions from "../coins/actions"

export function checkWhiteList() {
  return async (dispatch, getState, socket) => {
    dispatch(notificationActions.trace("Checking whitelist"))
    var result
    try {
      result = await authService.checkWhiteList()
    } catch (error) {
      dispatch(notificationActions.trace("Error checking whitelist"))
      dispatch({
        type: types.WHITELIST_UPDATE,
        error: true,
        payload: error
      })
      return
    }
    dispatch({ type: types.WHITELIST_UPDATE, payload: Boolean(result) })
    if (result) {
      dispatch(notificationActions.trace("Verified whitelist"))
      dispatch(attemptConnect())
    } else {
      dispatch(notificationActions.trace("Whitelist rejected, disconnecting"))
      socket.disconnect()
    }
  }
}

export function attemptConnect() {
  return async (dispatch, getState) => {
    dispatch(notificationActions.trace("Attempting connection"))
    var success = await authService.checkLoggedIn()
    if (success) {
      dispatch(notificationActions.trace("Logged in"))
      dispatch({ type: types.LOGIN, payload: {} })
      dispatch(connect())
    } else {
      dispatch(notificationActions.trace("Not logged in"))
      dispatch(fetchOktaConfig())
    }
  }
}

function connect() {
  return async (dispatch, getState, socket) => {
    var { config } = getState().auth
    if (config && config.clientId) {
      await dispatch(notificationActions.trace("Connecting using Okta"))
    } else {
      await dispatch(notificationActions.trace("Connecting using main auth"))
    }
    await dispatch(coinActions.fetch())
    await dispatch(coinActions.fetchReferencePrices())
    await socket.connect()
  }
}

export function whitelist(token) {
  return async (dispatch, getState, socket) => {
    try {
      dispatch(notificationActions.trace("Attempting whitelist"))
      await authService.whitelist(token)
      dispatch(notificationActions.trace("Accepted whitelist"))
      dispatch({ type: types.WHITELIST_UPDATE, payload: true })
      dispatch(attemptConnect())
    } catch (error) {
      dispatch(notificationActions.trace(error.message))
      dispatch({
        type: types.WHITELIST_UPDATE,
        error: true,
        payload: error
      })
    }
  }
}

export function clearWhitelist() {
  return async (dispatch, getState, socket) => {
    try {
      await authService.clearWhiteList()
    } catch (error) {
      dispatch(errorActions.setForeground(error.message))
      return
    }
    await dispatch({ type: types.WHITELIST_UPDATE, payload: false })
    await socket.disconnect()
    dispatch(checkWhiteList())
  }
}

function fetchOktaConfig() {
  return async (dispatch, getState, socket) => {
    dispatch(notificationActions.trace("Fetching auth config"))
    try {
      const config = await authService.config()
      dispatch(notificationActions.trace("Successfully fetched auth config"))
      dispatch({ type: types.SET_OKTA_CONFIG, payload: config })
    } catch (error) {
      notificationActions.localError(
        "Could not fetch authentication data: " + error.message
      )
      return
    }
  }
}

export function logout() {
  return (dispatch, getState, socket) => {
    dispatch({ type: types.LOGOUT })
    socket.disconnect()
    dispatch(checkWhiteList())
  }
}

export function login(details) {
  return async (dispatch, getState, socket) => {
    await dispatch(notificationActions.trace("Logged in successfully"))
    await dispatch({
      type: types.LOGIN,
      payload: details
    })
    await dispatch(connect())
  }
}

export function invalidateLogin() {
  return async (dispatch, getState, socket) => {
    await dispatch({ type: types.INVALIDATE_LOGIN })
    await socket.disconnect()
    dispatch(checkWhiteList())
  }
}

export function handleHttpResponse(response) {
  if (response.status === 403) {
    return {
      type: types.WHITELIST_UPDATE,
      error: true,
      payload: new Error("Whitelisting expired")
    }
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
        var errorMessage = null
        try {
          errorMessage = (await response.json()).message
        } catch (err) {
          // No-op
        }
        if (!errorMessage) {
          errorMessage = response.statusText
            ? response.statusText
            : "Server error (" + response.status + ")"
        }
        throw new Error(errorMessage)
      }
    } else {
      if (jsonHandler) dispatch(jsonHandler(await response.json()))
      if (onSuccess) dispatch(onSuccess())
    }
  } catch (error) {
    if (errorHandler) dispatch(errorHandler(error))
  }
}
