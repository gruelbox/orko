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
import React, { useState } from "react"
import { Loader, Dimmer } from "semantic-ui-react"
import Whitelisting from "components/Whitelisting"
import Login from "components/Login"
import LoggedIn from "components/LoggedIn"
//import AuthService from "../services/auth"

const AuthContainer = () => {
  //const [loading, setLoading] = useState(true)
  //const [loggedIn, setLoggedIn] = useState(false)
  //const [whitelisted, setWhitelisted] = useState(false)
  //const [error, setError] = useState<string>(undefined)

  const [loading] = useState(true)
  const [loggedIn] = useState(false)
  const [whitelisted] = useState(false)
  const [error] = useState<string>("")

  //et onWhitelist = (token: string) =>
  //  this.props.dispatch(actions.whitelist(token))
  //let onLogin = (details: any) => this.props.dispatch(actions.login(details))
  //let onError = (error: string) =>
  //  this.props.dispatch(errorActions.setForeground("Login error: " + error))

  //let onWhitelist = (token: string) => {}
  //let onLogin = (details: any) => {}
  //let onError = (error: string) => {}

  let onWhitelist = () => {}
  let onLogin = () => {}
  let onError = () => {}

  if (loading) {
    return (
      <Dimmer active={true}>
        <Loader active={true} />
      </Dimmer>
    )
  } else if (!whitelisted) {
    return <Whitelisting onApply={onWhitelist} error={error} />
  } else if (!loggedIn) {
    return <Login error={error} onSuccess={onLogin} onError={onError} />
  } else {
    return <LoggedIn />
  }
}

export default AuthContainer
