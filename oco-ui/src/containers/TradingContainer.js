import React from "react"
import TradeSelector from "../components/TradeSelector"

class TradingContainer extends React.Component {
  render() {
    return (
      <TradeSelector coin={this.props.coin}/>
    )
  }
}

export default TradingContainer