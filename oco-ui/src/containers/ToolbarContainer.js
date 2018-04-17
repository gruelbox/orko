import React from "react"
import { connect } from "react-redux"

import styled from "styled-components"
import { space } from "styled-system"

import { Toolbar } from "rebass"
import { Icon } from "semantic-ui-react"
import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"
import Span from "../components/primitives/Span"

import * as authActions from "../store/auth/actions"

const ToolbarBox = styled.div`
  background-color: ${props => props.theme.colors.toolbar} ${space};
`

const TickerSocketState = ({ connected }) => {
  return (
    <Span color={connected ? "black" : "red"} ml={4} fontWeight="bold">
      <Icon name="wifi" color={connected ? "black" : "red"} />
      {connected ? "Socket connected" : "Socket down"}
    </Span>
  )
}

const BackgroundErrors = ({ errors }) => {
  const errorKeys = Object.keys(errors)
  const hasErrors = errorKeys.length !== 0
  const errorString = hasErrors
    ? errorKeys.map(k => errors[k]).join(", ")
    : null
  return (
    <Span color={hasErrors ? "red" : "black"} ml={4} fontWeight="bold">
      <Icon name="wifi" color={hasErrors ? "red" : "black"} />
      {hasErrors ? errorString : "Connected"}
    </Span>
  )
}

const HomeLink = () => (
  <Link to="/" color="black" fontWeight="bold">
    Home
  </Link>
)

const SignOutLink = ({ onClick, userName }) => (
  <Span ml="auto" color="black">
    <Href color="black" fontWeight="bold" onClick={onClick}>
      Sign out
    </Href>
    &nbsp;({userName})
  </Span>
)

const InvalidateLink = ({ onClick }) => (
  <Href ml={4} color="black" fontWeight="bold" onClick={onClick}>
    Invalidate whitelist
  </Href>
)

const ToolbarContainer = props => {
  return (
    <div>
      <ToolbarBox>
        <Toolbar>
          <HomeLink />
          <TickerSocketState connected={props.connected} />
          <BackgroundErrors errors={props.errors} />
          <SignOutLink
            userName={props.userName}
            onClick={() => props.dispatch(authActions.logout())}
          />
          <InvalidateLink
            onClick={() => props.dispatch(authActions.clearWhitelist())}
          />
        </Toolbar>
      </ToolbarBox>
    </div>
  )
}

function mapStateToProps(state) {
  return {
    errors: state.error.errorBackground,
    userName: state.auth.userName,
    connected: state.ticker.connected
  }
}

export default connect(mapStateToProps)(ToolbarContainer)
