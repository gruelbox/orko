import React from "react"
import TradeSelector from "../components/TradeSelector"
import { getSelectedCoin } from "../selectors/coins"
import { connect } from "react-redux"

class TradingContainer extends React.Component {
  render() {
    return <TradeSelector coin={this.props.coin} />
  }
}

export default connect((state, props) => ({
  coin: getSelectedCoin(state)
}))(TradingContainer)
