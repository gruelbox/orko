import React from "react"
import { connect } from "react-redux"

import Toolbar from "../components/Toolbar"

import { formatNumber } from "../util/numberUtils"
import { getSelectedCoinTicker, getSelectedCoin } from "../selectors/coins"
import * as authActions from "../store/auth/actions"
import * as uiActions from "../store/ui/actions"

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
      onOpenScripts={() => props.dispatch(uiActions.openScripts())}
      onLogout={() => props.dispatch(authActions.logout())}
      onClearWhitelist={() => props.dispatch(authActions.clearWhitelist())}
    />
  )
}

export default connect((state, props) => {
  const coin = getSelectedCoin(state)
  return {
    errors: state.error.errorBackground,
    connected: state.socket.connected,
    updateFocusedField: state.focus.fn,
    ticker: getSelectedCoinTicker(state),
    coin,
    coinMetadata:
      coin && state.coins.meta ? state.coins.meta[coin.key] : undefined
  }
})(ToolbarContainer)
