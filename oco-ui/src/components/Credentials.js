import React, { Component } from "react"
import { Dimmer } from "semantic-ui-react"
import OktaSignInWidget from "./OktaSignInWidget"

export default class Credentials extends Component {
  onSuccess = res => {
    this.props.onGotToken(res[1].accessToken, res[0].claims.name)
  }

  render() {
    return (
      <Dimmer active>
        <OktaSignInWidget
          config={this.props.config}
          onSuccess={this.onSuccess}
          onError={this.props.onError}
        />
      </Dimmer>
    )
  }
}