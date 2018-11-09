import React, { Component } from "react"
import OktaSignInWidget from "./OktaSignInWidget"
import Login from "./Login"
import { Dimmer } from "semantic-ui-react"
import { unixToDate } from "../util/dateUtils"

export default class Credentials extends Component {
  render() {
    if (this.props.config.clientId) {
      return (
        <Dimmer active>
          <OktaSignInWidget
            config={this.props.config}
            onSuccess={res => {
              console.log("Response from Okta", res)
              this.props.onSuccess({
                token: res[1].accessToken,
                expiry: unixToDate(res[1].expiresAt)
              })
            }}
            onError={this.props.onError}
          />
        </Dimmer>
      )
    } else {
      return (
        <Login
          error={this.props.error}
          onSuccess={this.props.onSuccess}
          onError={this.props.onError}
        />
      )
    }
  }
}
