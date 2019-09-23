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
import {
  getSelectedCoinTicker,
  getSelectedCoin,
  getSelectedExchange
} from "../selectors/coins"
import { getHiddenPanels } from "../selectors/ui"
import { SocketContext } from "@orko-ui-socket/index"
import { Coin, Ticker } from "modules/market/Types"

interface ToolbarContainerProps {
  onLogout(): void
  onClearWhitelist(): void
  onTogglePanelVisible(key: string): void
  onShowViewSettings(): void
  width: number
  mobile: boolean
}

interface ToolbarReduxProps extends ToolbarContainerProps {
  version: string
  hiddenPanels: any // TODO
  errors: Array<string>
  updateFocusedField(): void
  ticker: Ticker
  balance: number
  coin: Coin
  coinMetadata: any // TODO
  exchangeMetadata: any // TODO
}

const ToolbarContainer: React.FC<ToolbarReduxProps> = props => {
  const socket = useContext(SocketContext)

  if (!socket.connected) {
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
  return (
    <Toolbar
      {...props}
      connected={socket.connected}
      onLogout={props.onLogout}
      onShowPanel={(key: string) => props.onTogglePanelVisible(key)}
      balance={props.balance}
      exchangeMetadata={props.exchangeMetadata}
    />
  )
}

export default connect((state, props) => {
  const coin = getSelectedCoin(state)
  return {
    version: state.support.meta.version,
    hiddenPanels: getHiddenPanels(state),
    errors: state.error.errorBackground,
    updateFocusedField: state.focus.fn,
    ticker: getSelectedCoinTicker(state),
    balance: state.coin.balance,
    coin,
    coinMetadata:
      coin && state.coins.meta ? state.coins.meta[coin.key] : undefined,
    exchangeMetadata: getSelectedExchange(state)
  }
})(ToolbarContainer)
