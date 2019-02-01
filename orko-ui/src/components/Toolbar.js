/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React from "react"

import styled from "styled-components"
import { space } from "styled-system"

import { Icon } from "semantic-ui-react"
import { Link } from "react-router-dom"
import Href from "./primitives/Href"
import Span from "./primitives/Span"

import Ticker from "./Ticker"
import Balance from "./Balance"

const ToolbarBox = styled.div`
  position: relative;
  display: flex;
  padding: 0 ${props => props.theme.space[2]}px 0
    ${props => props.theme.space[2]}px;
  align-items: center;
  height: 56px;
`

const VersionBox = styled.div`
  position: absolute;
  color: rgba(255, 255, 255, 0.2);
  bottom: 0;
  right: ${props => props.theme.space[2]}px;
`

const Logo = () => (
  <Link title="Home" to="/" fontWeight="bold">
    <h1 style={{ textShadow: "0 0 10px #FF0000" }}>
      <Span color="sell">O</Span>
      <Span color="white">rko</Span>
    </h1>
  </Link>
)

const Version = ({ version }) => <VersionBox>Version: {version}</VersionBox>

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

const ViewSettings = ({ onClick }) => (
  <Href
    ml={2}
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

const Panel = ({ panel, onClick, nomargin }) => (
  <Href
    ml={nomargin ? 0 : 2}
    fontSize={3}
    color="deemphasis"
    title={"Show " + panel.key}
    fontWeight="bold"
    onClick={onClick}
  >
    <Icon name={panel.icon} />
  </Href>
)

const ScriptsLink = () => (
  <Link
    ml={2}
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
    ml={2}
    fontWeight="bold"
    onClick={onClick}
  >
    <Icon name="moon" />
  </Href>
)

const RemainingSpace = styled.div`
  flex-shrink: 1;
  overflow: hidden;
  border-left: ${props =>
    props.noleftborder ? "none" : "1px solid rgba(255, 255, 255, 0.2)"};
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: ${props => props.theme.space[2]}px;
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

const Coin = ({ coin, exchangeMetadata }) => (
  <CoinContainer px={3} data-orko="selectedCoin">
    <CoinTicker>{coin ? coin.base + "/" + coin.counter : ""}</CoinTicker>
    <CoinExchange>
      {coin && exchangeMetadata ? exchangeMetadata.name : ""}
    </CoinExchange>
  </CoinContainer>
)

const Toolbar = ({
  ticker,
  coin,
  balance,
  connected,
  hiddenPanels,
  updateFocusedField,
  onShowViewSettings,
  onLogout,
  onClearWhitelist,
  onShowPanel,
  width,
  version,
  mobile,
  exchangeMetadata
}) => (
  <ToolbarBox p={0}>
    <Logo />
    <Version version={version} />
    <Coin coin={coin} exchangeMetadata={exchangeMetadata} />
    <RemainingSpace mx={2}>
      <Ticker
        mobile={mobile}
        coin={coin}
        ticker={ticker}
        onClickNumber={number => {
          if (updateFocusedField) {
            updateFocusedField(number)
          }
        }}
      />
    </RemainingSpace>
    {!mobile &&
      hiddenPanels
        .filter(p => p.key === "balance")
        .map(panel => (
          <>
            {width >= 1440 && (
              <RemainingSpace noleftborder>
                <Panel
                  nomargin
                  key={panel.key}
                  panel={panel}
                  onClick={() => onShowPanel(panel.key)}
                />
                <Balance coin={coin} balance={balance} ticker={ticker} />
              </RemainingSpace>
            )}
            {width < 1440 && (
              <Panel
                nomargin
                key={panel.key}
                panel={panel}
                onClick={() => onShowPanel(panel.key)}
              />
            )}
          </>
        ))}
    {!mobile &&
      hiddenPanels
        .filter(p => p.key !== "balance")
        .map(panel => (
          <Panel
            key={panel.key}
            panel={panel}
            onClick={() => onShowPanel(panel.key)}
          />
        ))}
    <TickerSocketState connected={connected} />
    <ScriptsLink />
    <ViewSettings onClick={onShowViewSettings} />
    <SignOutLink onClick={onLogout} />
    <InvalidateLink onClick={onClearWhitelist} />
  </ToolbarBox>
)

export default Toolbar
