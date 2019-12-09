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
import React, { useContext, useMemo } from "react"
import { connect } from "react-redux"

import Toolbar from "../components/Toolbar"

import { formatNumber } from "modules/common/util/numberUtils"
import { getSelectedCoin } from "../selectors/coins"
import { SocketContext } from "modules/socket"
import { Coin } from "modules/market"
import { Ticker } from "modules/socket"
import { MarketContext } from "modules/market"
import { Panel } from "useUiConfig"
import { FrameworkContext } from "FrameworkContainer"
import { ServerContext } from "modules/server"

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
  ticker: Ticker
  balance: number
  coin: Coin
}

const ToolbarContainer: React.FC<ToolbarReduxProps> = props => {
  const socket = useContext(SocketContext)
  const marketApi = useContext(MarketContext)
  const serverApi = useContext(ServerContext)

  const ticker = useContext(SocketContext).selectedCoinTicker
  const updateFocusedField = useContext(FrameworkContext).populateLastFocusedField

  const allMetadata = serverApi.coinMetadata
  const coinMetadata = useMemo(() => (props.coin ? allMetadata.get(props.coin.key) : null), [
    props.coin,
    allMetadata
  ])

  if (!socket.connected) {
    document.title = "Not connected"
  } else if (ticker && props.coin) {
    document.title =
      formatNumber(ticker.last, coinMetadata ? coinMetadata.priceScale : 8, "No price") +
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

export default connect(state => {
  return {
    version: state.support.meta.version,
    coin: getSelectedCoin(state)
  }
})(ToolbarContainer)
