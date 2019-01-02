import React, { Component } from "react"
import OktaSignInWidget from "./OktaSignInWidget"
import Login from "./Login"
import { Dimmer } from "semantic-ui-react"

export default class Credentials extends Component {
  render() {
    if (this.props.config.clientId) {
      return (
        <Dimmer active>
          <OktaSignInWidget
            config={this.props.config}
            onSuccess={this.props.onSuccess}
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
