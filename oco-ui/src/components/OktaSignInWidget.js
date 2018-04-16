import React, { Component } from "react"
import ReactDOM from "react-dom"
import OktaSignIn from "@okta/okta-signin-widget/dist/js/okta-sign-in.min.js"
import "@okta/okta-signin-widget/dist/css/okta-sign-in.min.css"
import "@okta/okta-signin-widget/dist/css/okta-theme.css"

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
    this.widget.renderEl({ el }, this.props.onSuccess, this.props.onError)
  }

  componentWillUnmount() {
    this.widget.remove()
  }

  render() {
    return <div />
  }
}
