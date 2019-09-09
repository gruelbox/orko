/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import authService from "../../services/auth"
import * as notificationActions from "../notifications/actions"
import * as errorActions from "../error/actions"
import * as coinActions from "../coins/actions"
import * as scriptActions from "../scripting/actions"
import * as supportActions from "../support/actions"
import * as exchangesActions from "../exchanges/actions"
import { clearXsrfToken } from "services/fetchUtil"

export function attemptConnect() {
  return async (dispatch, getState) => {
    dispatch(notificationActions.trace("Attempting connection"))
    var success = await authService.checkLoggedIn()
    if (success) {
      dispatch(notificationActions.trace("Logged in"))
      dispatch(connect())
    } else {
      dispatch(notificationActions.trace("Not logged in"))
      redirectToLogin()
    }
  }
}

function connect() {
  return async (dispatch, getState, socket) => {
    await dispatch(notificationActions.trace("Connecting"))
    var scriptsPromise = dispatch(scriptActions.fetch())
    var metaPromise = dispatch(supportActions.fetchMetadata())
    await dispatch(exchangesActions.fetchExchanges())
    await dispatch(coinActions.fetch())
    await dispatch(coinActions.fetchReferencePrices())
    await scriptsPromise
    await metaPromise
    await socket.connect()
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
    await socket.disconnect()
    redirectToLogin()
  }
}

export function logout() {
  return (dispatch, getState, socket) => {
    clearXsrfToken()
    socket.disconnect()
    redirectToLogin()
  }
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

function redirectToLogin() {
  console.log("API request failed. Redirecting to login")
  if (window.location.pathname.startsWith("/login")) {
    window.location.href = "/login"
  } else {
    window.location.href = "/login?redirectTo=" + window.location.pathname
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
    // Dispatch the request
    const response = await apiRequest(auth)

    if (!response.ok) {
      if (response.status === 403 || response.status === 401) {
        redirectToLogin()
      } else if (response.status !== 200) {
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
