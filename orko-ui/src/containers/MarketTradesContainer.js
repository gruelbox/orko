import React from "react"
import { connect } from "react-redux"
import TradeHistory from "../components/TradeHistory"
import Loading from "../components/primitives/Loading"
import { getMarketTradeHistory, getSelectedCoin } from "../selectors/coins"

class MarketTradesContainer extends React.Component {
  render() {
    return !this.props.tradeHistory ? (
      <Loading p={2} />
    ) : (
      <TradeHistory
        coin={this.props.coin}
        trades={this.props.tradeHistory}
        excludeFees={true}
      />
    )
  }
}

function mapStateToProps(state, props) {
  return {
    tradeHistory: getMarketTradeHistory(state),
    coin: getSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(MarketTradesContainer)
