import React from "react"
import { connect } from "react-redux"

import styled from "styled-components"
import { space } from "styled-system"

import { Toolbar } from "rebass"
import { Icon } from "semantic-ui-react"
import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"
import Span from "../components/primitives/Span"

import Ticker from "../components/Ticker"

import * as authActions from "../store/auth/actions"

const ToolbarBox = styled.div`
  background-color: ${props => props.theme.colors.backgrounds[4]}
  ${space};
`

const TickerSocketState = ({ connected }) => {
  return (
    <Span
      title={connected ? "Socket connected" : "Socket down"}
      color={connected ? "white" : "heading"}
      mx={2}
      fontWeight="bold"
    >
      <Icon name="wifi"/>
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
  <Link mr={2} color="heading" title="Home" fontSize={3} to="/" fontWeight="bold">
    <Icon name="home" />
  </Link>
)

const SignOutLink = ({ onClick, userName }) => (
  <Span ml="auto">
    <Href
      color="heading"
      fontSize={3}
      title="Sign out"
      fontWeight="bold"
      onClick={onClick}
    >
      <Icon name="sign out" />
    </Href>
  </Span>
)

const InvalidateLink = ({ onClick }) => (
  <Href
    color="heading"
    fontSize={3}
    title="Invalidate whitelist"
    ml={2}
    fontWeight="bold"
    onClick={onClick}
  >
    <Icon name="moon" />
  </Href>
)

const RemainingSpace = styled.div`
  flex-shrink: 1;
  overflow: auto;
  ${space}
`

const CoinContainer = styled.div`
  ${space}
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

const Coin = ({coin}) => (
  <CoinContainer px={3}>
    <CoinTicker>{coin ? coin.base + "/" + coin.counter : ""}</CoinTicker>
    <CoinExchange>{coin ? coin.exchange : ""}</CoinExchange>
  </CoinContainer>
)

const ToolbarContainer = props => {
  const ticker = (props.tickers && props.coin) ? props.tickers[props.coin.key] : null
  if (ticker && props.coin) {
    document.title = ticker.last + " " + props.coin.base + "/" + props.coin.counter 
  } else {
    document.title = "No coin"
  }
  return (
    <ToolbarBox p={0}>
      <Toolbar p={0} m={0}>
        <HomeLink />
        <TickerSocketState connected={props.connected} />
        <BackgroundErrors errors={props.errors} />
        <Coin coin={props.coin}/>
        <RemainingSpace mx={2}>
          <Ticker
            coin={props.coin}
            ticker={props.coin ? props.tickers[props.coin.key] : null}
            onClickNumber={number => {
              if (props.updateFocusedField) {
                props.updateFocusedField(number)
              }
            }}
          />
        </RemainingSpace>
        <SignOutLink
          userName={props.userName}
          onClick={() => props.dispatch(authActions.logout())}
        />
        <InvalidateLink
          onClick={() => props.dispatch(authActions.clearWhitelist())}
        />
      </Toolbar>
    </ToolbarBox>
  )
}

function mapStateToProps(state) {
  return {
    errors: state.error.errorBackground,
    userName: state.auth.userName,
    connected: state.ticker.connected,
    updateFocusedField: state.focus.fn,
    tickers: state.ticker.coins
  }
}

export default connect(mapStateToProps)(ToolbarContainer)