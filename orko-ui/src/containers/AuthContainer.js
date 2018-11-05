import React, { Component } from "react"
import { connect } from "react-redux"
import * as actions from "../store/auth/actions"
import * as errorActions from "../store/error/actions"
import { Loader } from "semantic-ui-react"
import Whitelisting from "../components/Whitelisting"
import Credentials from "../components/Credentials"

class AuthContainer extends Component {
  componentDidMount() {
    this.props.dispatch(actions.checkWhiteList())
  }

  onWhitelist = token => {
    this.props.dispatch(actions.whitelist(token))
  }

  render() {
    if (this.props.auth.loading) {
      return <Loader active={true} />
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
          onGotToken={(token, userName) =>
            this.props.dispatch(actions.setToken(token, userName))
          }
          onError={error =>
            this.props.dispatch(
              errorActions.setForeground("Login error: " + error)
            )
          }
        />
      )
    } else if (!this.props.auth.loggedIn && !this.props.auth.config) {
      return <Loader active={true} />
    } else {
      return this.props.children
    }
  }
}

function mapStateToProps(state) {
  return {
    auth: state.auth
  }
}

export default connect(mapStateToProps)(AuthContainer)
