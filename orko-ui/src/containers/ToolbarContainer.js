import React from "react"
import { connect } from "react-redux"

import styled from "styled-components"
import { space } from "styled-system"

import { Icon } from "semantic-ui-react"
import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"
import Span from "../components/primitives/Span"

import Ticker from "../components/Ticker"

import { formatNumber } from "../util/numberUtils"
import * as authActions from "../store/auth/actions"
import { getSelectedCoinTicker } from "../selectors/coins"

const ToolbarBox = styled.div`
  display: flex;
  padding: 0;
  align-items: center;
  height: 56px;
`

const Logo = () => (
  <div>
    <h1><Span color="sell">O</Span>r<Span color="buy">k</Span>o</h1>
  </div>
)

const TickerSocketState = ({ connected }) => {
  return (
    <Span
      title={connected ? "Socket connected" : "Socket down"}
      color={connected ? "white" : "red"}
      ml="auto"
      mr={2}
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

const ViewSettings = ({ onClick }) => (
  <Href
    mx={2}
    onClick={onClick}
    color="heading"
    title="View settings"
    fontSize={3}
    fontWeight="bold"
  >
    <Icon name="eye" />
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

const Normal = ({
  ticker,
  coin,
  connected,
  errors,
  userName,
  updateFocusedField,
  onShowViewSettings,
  dispatch
}) => (
  <ToolbarBox p={0}>
    <HomeLink />
    <Logo />
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
    <TickerSocketState connected={connected} />
    <ViewSettings onClick={onShowViewSettings} />
    <SignOutLink
      userName={userName}
      onClick={() => dispatch(authActions.logout())}
    />
    <InvalidateLink onClick={() => dispatch(authActions.clearWhitelist())} />
  </ToolbarBox>
)

const Mobile = ({
  ticker,
  coin,
  connected,
  errors,
  userName,
  updateFocusedField,
  onResetLayout,
  dispatch
}) => (
  <ToolbarBox>
    <HomeLink />
    <TickerSocketState connected={connected} />
    <Coin coin={coin} />
    <SignOutLink
      userName={userName}
      onClick={() => dispatch(authActions.logout())}
    />
    <InvalidateLink onClick={() => dispatch(authActions.clearWhitelist())} />
  </ToolbarBox>
)

const ToolbarContainer = props => {
  if (!props.connected) {
    document.title = "Not connected"
  } else if (props.ticker && props.coin) {
    document.title =
      formatNumber(
        props.ticker.last,
        props.coinMetadata ? props.coinMetadata.priceScale : 8,
        "No price"
      ) +
      " " +
      props.coin.base +
      "/" +
      props.coin.counter
  } else {
    document.title = "No coin"
  }
  return props.mobile ? <Mobile {...props} /> : <Normal {...props} />
}

export default connect((state, props) => ({
  errors: state.error.errorBackground,
  userName: state.auth.userName,
  connected: state.socket.connected,
  updateFocusedField: state.focus.fn,
  ticker: getSelectedCoinTicker(state),
  coinMetadata:
    props.coin && state.coins.meta
      ? state.coins.meta[props.coin.key]
      : undefined
}))(ToolbarContainer)
