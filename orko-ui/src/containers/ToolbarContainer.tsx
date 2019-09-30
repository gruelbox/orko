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
import React, { useContext } from "react"
import { connect } from "react-redux"

import Toolbar from "../components/Toolbar"

import { formatNumber } from "@orko-ui-common/util/numberUtils"
import { getSelectedCoin } from "../selectors/coins"
import { SocketContext } from "@orko-ui-socket/index"
import { Coin } from "modules/market/index"
import { Ticker } from "modules/socket/index"
import { MarketContext } from "@orko-ui-market/index"
import { Panel } from "useUiConfig"
import { FrameworkContext } from "FrameworkContainer"

interface ToolbarContainerProps {
  onLogout(): void
  onClearWhitelist(): void
  onTogglePanelVisible(key: string): void
  onShowViewSettings(): void
  width: number
  mobile: boolean
  hiddenPanels: Panel[]
}

interface ToolbarReduxProps extends ToolbarContainerProps {
  version: string
  errors: Array<string>
  ticker: Ticker
  balance: number
  coin: Coin
  coinMetadata: any // TODO
}

const ToolbarContainer: React.FC<ToolbarReduxProps> = props => {
  const socket = useContext(SocketContext)
  const marketApi = useContext(MarketContext)
  const ticker = useContext(SocketContext).selectedCoinTicker
  const updateFocusedField = useContext(FrameworkContext).populateLastFocusedField

  if (!socket.connected) {
    document.title = "Not connected"
  } else if (ticker && props.coin) {
    document.title =
      formatNumber(ticker.last, props.coinMetadata ? props.coinMetadata.priceScale : 8, "No price") +
      " " +
      props.coin.base +
      "/" +
      props.coin.counter
  } else {
    document.title = "No coin"
  }
  return (
    <Toolbar
      {...props}
      updateFocusedField={updateFocusedField}
      ticker={ticker}
      connected={socket.connected}
      onLogout={props.onLogout}
      onShowPanel={(key: string) => props.onTogglePanelVisible(key)}
      balance={socket.balances}
      exchangeMetadata={marketApi.data.selectedExchange}
    />
  )
}

export default connect((state, props) => {
  const coin = getSelectedCoin(state)
  return {
    version: state.support.meta.version,
    errors: state.error.errorBackground,
    coin,
    coinMetadata: coin && state.coins.meta ? state.coins.meta[coin.key] : undefined
  }
})(ToolbarContainer)
