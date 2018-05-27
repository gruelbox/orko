import React from "react"
import { connect } from "react-redux"

import styled from "styled-components"
import { space } from "styled-system"

import { Icon } from "semantic-ui-react"
import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"
import Span from "../components/primitives/Span"

import Ticker from "../components/Ticker"

import * as authActions from "../store/auth/actions"
import { getSelectedCoinTicker } from "../selectors/coins"

const ToolbarBox = styled.div`
  background-color: ${props => props.theme.colors.backgrounds[2]};
  display: flex;
  padding: 0;
  align-items: center;
  height: 56px;
  ${space};
`

const TickerSocketState = ({ connected }) => {
  return (
    <Span
      title={connected ? "Socket connected" : "Socket down"}
      color={connected ? "white" : "red"}
      mx={2}
      fontWeight="bold"
    >
      <Icon name="wifi" />
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
    <Span
      title={hasErrors ? errorString : "Connected"}
      color={hasErrors ? "red" : "white"}
      mx={2}
      fontWeight="bold"
    >
      <Icon name="wifi" />
    </Span>
  )
}

const HomeLink = () => (
  <Link
    mx={2}
    color="heading"
    title="Home"
    fontSize={3}
    to="/"
    fontWeight="bold"
  >
    <Icon name="home" />
  </Link>
)

const ResetLayout = ({onClick}) => (
  <Href
    ml="auto"
    onClick={onClick}
    color="heading"
    title="Reset layout to defaults"
    fontSize={3}
    fontWeight="bold"
  >
    <Icon name="fast backward" />
  </Href>
)

const SignOutLink = ({ onClick, userName }) => (
  <Href
    ml={2}
    color="heading"
    fontSize={3}
    title="Sign out"
    fontWeight="bold"
    onClick={onClick}
  >
    <Icon name="sign out" />
  </Href>
)

const InvalidateLink = ({ onClick }) => (
  <Href
    color="heading"
    fontSize={3}
    title="Invalidate whitelist"
    mx={2}
    fontWeight="bold"
    onClick={onClick}
  >
    <Icon name="moon" />
  </Href>
)

const RemainingSpace = styled.div`
  flex-shrink: 1;
  overflow: auto;
  ${space};
`

const CoinContainer = styled.div`
  ${space};
`

const CoinTicker = styled.h1`
  margin: 0;
  font-weight: bold;
  font-size: 15px;
`

const CoinExchange = styled.h2`
  margin: 0;
  font-size: 11px;
  font-weight: normal;
  color: ${props => props.theme.colors.fore};
`

const Coin = ({ coin }) => (
  <CoinContainer px={3}>
    <CoinTicker>{coin ? coin.base + "/" + coin.counter : ""}</CoinTicker>
    <CoinExchange>{coin ? coin.exchange : ""}</CoinExchange>
  </CoinContainer>
)

const ToolbarContainer = ({ticker, coin, connected, errors, userName, updateFocusedField, onResetLayout, dispatch}) => {
  if (!connected) {
    document.title = "Not connected"
  } else if (ticker && coin) {
    document.title = ticker.last + " " + coin.base + "/" + coin.counter
  } else {
    document.title = "No coin"
  }
  return (
    <ToolbarBox p={0}>
      <HomeLink />
      <TickerSocketState connected={connected} />
      <BackgroundErrors errors={errors} />
      <Coin coin={coin} />
      <RemainingSpace mx={2}>
        <Ticker
          coin={coin}
          ticker={ticker}
          onClickNumber={number => {
            if (updateFocusedField) {
              updateFocusedField(number)
            }
          }}
        />
      </RemainingSpace>
      <ResetLayout onClick={onResetLayout} />
      <SignOutLink
        userName={userName}
        onClick={() => dispatch(authActions.logout())}
      />
      <InvalidateLink
        onClick={() => dispatch(authActions.clearWhitelist())}
      />
    </ToolbarBox>
  )
}

function mapStateToProps(state) {
  return {
    errors: state.error.errorBackground,
    userName: state.auth.userName,
    connected: state.ticker.connected,
    updateFocusedField: state.focus.fn,
    ticker: getSelectedCoinTicker(state)
  }
}

export default connect(mapStateToProps)(ToolbarContainer)
