import React, { Component } from "react"
import OktaSignInWidget from "./OktaSignInWidget"
import Login from "./Login"
import { Dimmer } from "semantic-ui-react"

export default class Credentials extends Component {
  onSuccess = res => {
    this.props.onGotToken(res[1].accessToken, res[0].claims.name)
  }

  render() {
    if (this.props.config.clientId) {
      return (
        <Dimmer active>
          <OktaSignInWidget
            config={this.props.config}
            onSuccess={this.onSuccess}
            onError={this.props.onError}
          />
        </Dimmer>
      )
    } else {
      return (
        <Login
          error={this.props.error}
          onSuccess={this.props.onGotToken}
          onError={this.props.onError}
        />
      )
    }
  }
}
