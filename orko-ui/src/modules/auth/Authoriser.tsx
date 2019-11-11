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
import React, { useState, useEffect, ReactElement, useCallback, useMemo } from "react"
import { Loader, Dimmer } from "semantic-ui-react"
import Whitelisting from "./Whitelisting"
import Login from "./Login"
import LoginDetails from "./LoginDetails"
import authService from "./auth"
import { setXsrfToken, clearXsrfToken } from "modules/common/util/fetchUtil"
import {
  AuthContext,
  AuthApi,
  AuthenticatedRequestResponseType,
  AuthenticatedRequestOptions
} from "./AuthContext"

export interface AuthorizerProps {
  onError?(message: string): void
  children: ReactElement
}

/**
 * Self-contained authorisation/login component. Displays child components
 * only if authorised, otherwise shows the relevant login components.
 *
 * Provides a context API for logging out and performing API calls, handling
 * authentication errors by logging out.
 *
 * @param props
 */
export const Authorizer: React.FC<AuthorizerProps> = (props: AuthorizerProps) => {
  const [loading, setLoading] = useState(true)
  const [loggedIn, setLoggedIn] = useState(false)
  const [whitelisted, setWhitelisted] = useState(false)
  const [error, setError] = useState<string>(undefined)
  const authorised = whitelisted && loggedIn

  const propsOnError = props.onError
  const onError = useMemo(
    () => (message: string) => {
      setError(message)
      if (propsOnError) propsOnError(message)
    },
    [setError, propsOnError]
  )

  const checkConnected = useCallback(
    () =>
      (async function(): Promise<boolean> {
        console.log("Testing access")
        const success: boolean = await authService.checkLoggedIn()
        if (success) {
          console.log("Logged in")
        } else {
          console.log("Not logged in")
        }
        if (success) {
          setWhitelisted(true)
          setLoggedIn(true)
          setError(null)
        }
        return success
      })(),
    [setWhitelisted, setLoggedIn, setError]
  )

  const onWhitelist = useMemo(
    () =>
      async function(token: string): Promise<void> {
        try {
          console.log("Checking whitelist")
          await authService.whitelist(token)
          console.log("Accepted whitelist")
          setWhitelisted(true)
          setError(null)
          await checkConnected()
        } catch (error) {
          console.log(error.message)
          setWhitelisted(false)
          onError(`Whitelisting failed: ${error.message}`)
        }
      },
    [checkConnected, setWhitelisted, setError, onError]
  )

  const onLogin = useMemo(
    () =>
      async function(details: LoginDetails): Promise<void> {
        authService
          .simpleLogin(details)
          .then(({ expiry, xsrf }) => {
            try {
              console.log("Setting XSRF token")
              setXsrfToken(xsrf)
            } catch (error) {
              throw new Error("Malformed access token")
            }
            setLoggedIn(true)
            setError(null)
          })
          .then(checkConnected)
          .catch(error => {
            console.log(`Login failed: ${error.message}`)
            onError(error.message)
          })
      },
    [checkConnected, setLoggedIn, setError, onError]
  )

  const clearWhitelisting = useMemo(
    () =>
      async function(): Promise<void> {
        console.log("Clearing whitelist")
        try {
          await authService.clearWhiteList()
        } catch (error) {
          console.log(error.message)
          onError(error.message)
          return
        }
        setWhitelisted(false)
      },
    [setWhitelisted, onError]
  )

  const logout = useMemo(
    () =>
      function(): void {
        console.log("Logging out")
        clearXsrfToken()
        setLoggedIn(false)
      },
    [setLoggedIn]
  )

  const authenticatedRequest = useMemo(
    () => async <T extends unknown>(
      responseGenerator: () => Promise<Response>,
      options: AuthenticatedRequestOptions = { responseType: AuthenticatedRequestResponseType.JSON }
    ): Promise<T> => {
      const response = await responseGenerator()
      if (!response.ok) {
        var errorMessage = null
        if (response.status === 403) {
          console.log("Failed API request due to invalid whitelisting")
          setWhitelisted(false)
        } else if (response.status === 401) {
          console.log("Failed API request due to invalid token/XSRF")
          logout()
        } else {
          try {
            errorMessage = (await response.json()).message
          } catch (err) {
            errorMessage = response.statusText
              ? response.statusText
              : "Server error (" + response.status + ")"
          }
          console.log(errorMessage)
          throw new Error(errorMessage)
        }
      } else {
        switch (options.responseType) {
          case AuthenticatedRequestResponseType.JSON:
            return await response.json()
          case AuthenticatedRequestResponseType.TEXT:
            return (await response.text()) as T
          default:
            return null
        }
      }
    },
    [logout]
  )

  // TODO the presence of this as a thunk action is a transitionary
  // phase in moving entirely to context-based state
  const wrappedRequest = useMemo(
    () => (apiRequest, jsonHandler, errorHandler, onSuccess) => {
      return async (dispatch, getState) => {
        try {
          // Dispatch the request
          const response = await apiRequest()
          if (!response.ok) {
            if (response.status === 403) {
              console.log("Failed API request due to invalid whitelisting")
              setWhitelisted(false)
            } else if (response.status === 401) {
              console.log("Failed API request due to invalid token/XSRF")
              logout()
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
            if (jsonHandler) {
              const jh = jsonHandler(await response.json())
              if (jh) dispatch(jh)
            }
            if (onSuccess) {
              const os = onSuccess()
              if (os) dispatch(os)
            }
          }
        } catch (error) {
          if (errorHandler) {
            const eh = errorHandler(error)
            if (eh) dispatch(eh)
          }
        }
      }
    },
    [logout]
  )

  const api: AuthApi = useMemo(
    () => ({
      authorised,
      logout,
      clearWhitelisting,
      wrappedRequest,
      authenticatedRequest
    }),
    [authorised, logout, clearWhitelisting, wrappedRequest, authenticatedRequest]
  )

  // On mount, go through a full connection check
  useEffect(() => {
    const doSetup = async function(): Promise<void> {
      if (!(await checkConnected())) {
        console.log("Checking whitelist")
        try {
          const result = await authService.checkWhiteList()
          console.log(`Returned ${result}`)
          if (Boolean(result)) {
            console.log("Verified whitelist")
            setWhitelisted(true)
            setError(null)
            await checkConnected()
          } else {
            console.log("Not whitelisted")
            setWhitelisted(false)
            setError(null)
          }
        } catch (error) {
          console.log("Error checking whitelist")
          setWhitelisted(false)
          onError(error.message)
        }
      }
    }
    doSetup().finally(() => setLoading(false))
  }, [checkConnected, onError])

  if (loading) {
    return (
      <Dimmer active={true}>
        <Loader active={true} />
      </Dimmer>
    )
  } else if (!whitelisted) {
    return <Whitelisting onApply={onWhitelist} error={error} />
  } else if (!loggedIn) {
    return <Login error={error} onLogin={onLogin} />
  } else {
    return <AuthContext.Provider value={api}>{props.children}</AuthContext.Provider>
  }
}
