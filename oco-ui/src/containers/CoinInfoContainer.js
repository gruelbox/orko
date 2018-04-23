import React from "react"
import { connect } from "react-redux"
import CoinInfo from "../components/CoinInfo"
import * as tickerActions from "../store/ticker/actions"

class CoinInfoContainer extends React.Component {
  render() {
    return (
      <CoinInfo
        coin={this.props.coin}
        balance={this.props.balance}
        ticker={this.props.tickers[this.props.coin.key]}
        loading={false}
        onClickNumber={number => {
          if (this.props.updateFocusedField) {
            this.props.updateFocusedField(number)
          }
        }}
      />
    )
  }
}

function mapStateToProps(state) {
  return {
    balance: state.coin.balance,
    tickers: state.ticker.coins,
    updateFocusedField: state.focus.fn
  }
}

export default connect(mapStateToProps)(CoinInfoContainer)
