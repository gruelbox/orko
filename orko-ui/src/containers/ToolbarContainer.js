/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
import React from "react"
import { connect } from "react-redux"

import Toolbar from "../components/Toolbar"

import { formatNumber } from "../util/numberUtils"
import { getSelectedCoinTicker, getSelectedCoin } from "../selectors/coins"
import { getHiddenPanels } from "../selectors/ui"
import * as authActions from "../store/auth/actions"

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
  return (
    <Toolbar
      {...props}
      onLogout={() => props.dispatch(authActions.logout())}
      onClearWhitelist={() => props.dispatch(authActions.clearWhitelist())}
      onShowPanel={key => props.onTogglePanelVisible(key)}
      balance={props.balance}
    />
  )
}

export default connect((state, props) => {
  const coin = getSelectedCoin(state)
  return {
    version: state.support.meta.version,
    hiddenPanels: getHiddenPanels(state),
    errors: state.error.errorBackground,
    connected: state.socket.connected,
    updateFocusedField: state.focus.fn,
    ticker: getSelectedCoinTicker(state),
    balance: state.coin.balance,
    coin,
    coinMetadata:
      coin && state.coins.meta ? state.coins.meta[coin.key] : undefined
  }
})(ToolbarContainer)
