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
import React, { useState, useEffect, ReactElement } from "react"
import { Loader, Dimmer } from "semantic-ui-react"
import Whitelisting from "./components/Whitelisting"
import Login from "./components/Login"
import LoginDetails from "./models/LoginDetails"
import authService from "./services/auth"
import { setXsrfToken, clearXsrfToken } from "@orko-ui-common/util/fetchUtil"

export interface AuthContextFeatures {
  logout(): void
  clearWhitelisting(): void
  wrappedRequest(apiRequest, jsonHandler, errorHandler, onSuccess?)
}

export const AuthContext: React.Context<
  AuthContextFeatures
> = React.createContext({
  logout: () => {},
  clearWhitelisting: () => {}
})

interface AuthorizerProps {
  onMessage?(message: string): void
  onConnect?(): void
  onDisconnect?(): void
  children: ReactElement
}

const Authorizer: React.FC<AuthorizerProps> = (props: AuthorizerProps) => {
  const [loading, setLoading] = useState(true)
  const [loggedIn, setLoggedIn] = useState(false)
  const [whitelisted, setWhitelisted] = useState(false)
  const [error, setError] = useState<string>(undefined)

  const log = !props.onMessage
    ? (message: string) => console.log(message)
    : props.onMessage

  const checkConnected = async function(): Promise<boolean> {
    log("Testing access")
    const success: boolean = await authService.checkLoggedIn()
    if (success) {
      log("Logged in")
    } else {
      log("Not logged in")
    }
    if (success) {
      setWhitelisted(true)
      setLoggedIn(true)
      setError(null)
      props.onConnect && props.onConnect()
    }
    return success
  }

  const onWhitelist = async function(token: string): Promise<void> {
    try {
      log("Checking whitelist")
      await authService.whitelist(token)
      log("Accepted whitelist")
      setWhitelisted(true)
      setError(null)
      await checkConnected()
    } catch (error) {
      log(error.message)
      setWhitelisted(false)
      setError(`Whitelisting failed: ${error.message}`)
    }
  }

  const onLogin = async function(details: LoginDetails): Promise<void> {
    authService
      .simpleLogin(details)
      .then(({ expiry, xsrf }) => {
        try {
          log("Setting XSRF token")
          setXsrfToken(xsrf)
        } catch (error) {
          throw new Error("Malformed access token")
        }
        setLoggedIn(true)
        setError(null)
      })
      .then(checkConnected)
      .catch(error => {
        log(`Login failed: ${error.message}`)
        setError(error.message)
      })
  }

  const clearWhitelisting = async function(): Promise<void> {
    log("Clearing whitelist")
    try {
      await authService.clearWhiteList()
    } catch (error) {
      log(error.message)
      return
    }
    setWhitelisted(false)
    props.onDisconnect && props.onDisconnect()
  }

  const logout = function(): void {
    log("Logging out")
    clearXsrfToken()
    setLoggedIn(false)
    props.onDisconnect && props.onDisconnect()
  }

  const wrappedRequest = (apiRequest, jsonHandler, errorHandler, onSuccess) => {
    return async (dispatch, getState) => {
      dispatchWrappedRequest(
        dispatch,
        apiRequest,
        jsonHandler,
        errorHandler,
        onSuccess
      )
    }
  }

  const dispatchWrappedRequest = async (
    dispatch,
    apiRequest,
    jsonHandler,
    errorHandler,
    onSuccess
  ) => {
    try {
      // Dispatch the request
      const response = await apiRequest()
      if (!response.ok) {
        if (response.status === 403) {
          log("Failed API request due to invalid whitelisting")
          clearWhitelisting()
        } else if (response.status === 401) {
          log("Failed API request due to invalid token/XSRF")
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
        if (jsonHandler) dispatch(jsonHandler(await response.json()))
        if (onSuccess) dispatch(onSuccess())
      }
    } catch (error) {
      if (errorHandler) dispatch(errorHandler(error))
    }
  }

  useEffect(() => {
    const doSetup = async function(): Promise<void> {
      if (!(await checkConnected())) {
        log("Checking whitelist")
        try {
          const result = await authService.checkWhiteList()
          log(`Returned ${result}`)
          if (Boolean(result)) {
            log("Verified whitelist")
            setWhitelisted(true)
            setError(null)
            await checkConnected()
          } else {
            log("Not whitelisted")
            setWhitelisted(false)
            setError(null)
          }
        } catch (error) {
          log("Error checking whitelist")
          setWhitelisted(false)
          setError(error.message)
        }
      }
      setLoading(false)
    }
    doSetup()
    // eslint-disable-next-line
  }, [])

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
    return (
      <AuthContext.Provider
        value={{ logout, clearWhitelisting, wrappedRequest }}
      >
        {props.children}
      </AuthContext.Provider>
    )
  }
}

export default Authorizer
