/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
import React, { Component } from "react"
import ReactDOM from "react-dom"
import OktaSignIn from "@okta/okta-signin-widget/dist/js/okta-sign-in.min.js"
import "@okta/okta-signin-widget/dist/css/okta-sign-in.min.css"
import "@okta/okta-signin-widget/dist/css/okta-theme.css"
import { unixToDate } from "../util/dateUtils"
import { setAccessToken, setXsrfToken } from "../services/fetchUtil"

export default class OktaSignInWidget extends Component {
  componentDidMount() {
    const el = ReactDOM.findDOMNode(this)
    this.widget = new OktaSignIn({
      baseUrl: this.props.config.baseUrl,
      clientId: this.props.config.clientId,
      redirectUri: window.location.origin,
      authParams: {
        issuer: this.props.config.issuer,
        responseType: ["id_token", "token"],
        scopes: ["openid", "profile", "email"]
      }
    })
    this.widget.renderEl({ el }, this.onSuccess, this.props.onError)
  }

  componentWillUnmount() {
    this.widget.remove()
  }

  render() {
    return <div />
  }

  onSuccess = res => {
    setAccessToken(res[1].accessToken, unixToDate(res[1].expiresAt))
    setXsrfToken(undefined)
    this.props.onSuccess()
  }
}
