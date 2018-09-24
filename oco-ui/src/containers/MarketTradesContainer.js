import React from "react"
import { connect } from "react-redux"
import TradeHistory from "../components/TradeHistory"
import Loading from "../components/primitives/Loading"
import { getMarketTradeHistory } from "../selectors/coins"

class MarketTradesContainer extends React.Component {
  render() {
    return !this.props.tradeHistory ? (
      <Loading p={2} />
    ) : (
      <TradeHistory trades={this.props.tradeHistory} />
    )
  }
}

function mapStateToProps(state, props) {
  return {
    tradeHistory: getMarketTradeHistory(state)
  }
}

export default connect(mapStateToProps)(MarketTradesContainer)