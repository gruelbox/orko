import React from "react"

import styled from "styled-components"
import { space } from "styled-system"

import { Icon } from "semantic-ui-react"
import Link from "./primitives/Link"
import Href from "./primitives/Href"
import Span from "./primitives/Span"

import Ticker from "./Ticker"

const ToolbarBox = styled.div`
  display: flex;
  padding: 0;
  align-items: center;
  height: 56px;
`

const Logo = () => (
  <div>
    <h1 style={{ textShadow: "0 0 10px #FF0000" }}>
      <Span color="sell">O</Span>
      rko
    </h1>
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

const SignOutLink = ({ onClick }) => (
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

const ScriptsLink = () => (
  <Link
    mx={2}
    color="heading"
    fontSize={3}
    title="Open scripts"
    fontWeight="bold"
    to="/scripts"
  >
    <Icon name="code" />
  </Link>
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
  <CoinContainer px={3} data-orko="selectedCoin">
    <CoinTicker>{coin ? coin.base + "/" + coin.counter : ""}</CoinTicker>
    <CoinExchange>{coin ? coin.exchange : ""}</CoinExchange>
  </CoinContainer>
)

const Normal = ({
  ticker,
  coin,
  connected,
  updateFocusedField,
  onShowViewSettings,
  onOpenScripts,
  onLogout,
  onClearWhitelist
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
    <ScriptsLink />
    <ViewSettings onClick={onShowViewSettings} />
    <SignOutLink onClick={onLogout} />
    <InvalidateLink onClick={onClearWhitelist} />
  </ToolbarBox>
)

const Mobile = ({
  coin,
  connected,
  onOpenScripts,
  onLogout,
  onClearWhitelist
}) => (
  <ToolbarBox>
    <HomeLink />
    <TickerSocketState connected={connected} />
    <Coin coin={coin} />
    <ScriptsLink />
    <SignOutLink onClick={onLogout} />
    <InvalidateLink onClick={onClearWhitelist} />
  </ToolbarBox>
)

const Toolbar = props => {
  return props.mobile ? <Mobile {...props} /> : <Normal {...props} />
}

export default Toolbar
