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
import React, { useState, useEffect } from "react"
import { Loader, Dimmer } from "semantic-ui-react"
import Whitelisting from "components/Whitelisting"
import Login from "components/Login"
import LoginDetails from "models/LoginDetails"
import authService from "services/auth"
import { setXsrfToken } from "@orko-js-common/util/fetchUtil"
import LoggedIn from "components/LoggedIn"

const REDIRECT_TO = new URLSearchParams(window.location.search).get(
  "redirectTo"
)

const AuthContainer: React.FC<any> = () => {
  const [loading, setLoading] = useState(true)
  const [loggedIn, setLoggedIn] = useState(false)
  const [whitelisted, setWhitelisted] = useState(false)
  const [error, setError] = useState<string>(undefined)

  const checkConnected = async function(): Promise<boolean> {
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
  }

  const onWhitelist = async function(token: string) {
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
      setError(error.message)
    }
  }

  const onLogin = async function(details: LoginDetails) {
    authService
      .simpleLogin(details)
      .then(({ expiry, xsrf }) => {
        try {
          setXsrfToken(xsrf)
        } catch (error) {
          throw new Error("Malformed access token")
        }
        setLoggedIn(true)
        setError(null)
      })
      .catch(error => {
        console.log("Login failed", error.message)
        setError(error.message)
      })
  }

  useEffect(() => {
    const onSetup = async function() {
      if (!(await checkConnected())) {
        console.log("Checking whitelist")
        try {
          const result = await authService.checkWhiteList()
          console.log("Returned", result)
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
          setError(error.message)
        }
      }
      setLoading(false)
    }

    onSetup()
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
  } else if (!REDIRECT_TO) {
    return <LoggedIn />
  } else {
    console.log("Logged in. Redirecting")
    window.location.href = REDIRECT_TO
  }
}

export default AuthContainer
