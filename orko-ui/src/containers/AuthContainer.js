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
import React, { Component } from "react"
import { connect } from "react-redux"
import * as actions from "../store/auth/actions"
import * as errorActions from "../store/error/actions"
import { Loader, Dimmer } from "semantic-ui-react"
import Whitelisting from "../components/Whitelisting"
import Credentials from "../components/Credentials"

class AuthContainer extends Component {
  onWhitelist = token => {
    this.props.dispatch(actions.whitelist(token))
  }

  render() {
    if (this.props.auth.loading) {
      return (
        <Dimmer>
          <Loader active={true} />
        </Dimmer>
      )
    } else if (!this.props.auth.whitelisted) {
      return (
        <Whitelisting
          onApply={this.onWhitelist}
          error={this.props.auth.error}
        />
      )
    } else if (!this.props.auth.loggedIn && this.props.auth.config) {
      return (
        <Credentials
          error={this.props.auth.error}
          config={this.props.auth.config}
          onSuccess={details => this.props.dispatch(actions.login(details))}
          onError={error =>
            this.props.dispatch(
              errorActions.setForeground("Login error: " + error)
            )
          }
        />
      )
    } else if (!this.props.auth.loggedIn && !this.props.auth.config) {
      return (
        <Dimmer>
          <Loader active={true} />
        </Dimmer>
      )
    } else {
      return null
    }
  }
}

function mapStateToProps(state) {
  return {
    auth: state.auth
  }
}

export default connect(mapStateToProps)(AuthContainer)
