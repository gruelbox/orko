import React from "react"
import { connect } from "react-redux"
import TradeHistory from "../components/TradeHistory"
import Loading from "../components/primitives/Loading"
import { getUserTradeHistory, getSelectedCoin } from "../selectors/coins"

class UserTradeHistoryContainer extends React.Component {
  render() {
    return !this.props.tradeHistory ? (
      <Loading p={2} />
    ) : (
      <TradeHistory coin={this.props.coin} trades={this.props.tradeHistory} />
    )
  }
}

function mapStateToProps(state, props) {
  return {
    tradeHistory: getUserTradeHistory(state),
    coin: getSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(UserTradeHistoryContainer)
