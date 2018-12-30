import React from "react"
import { connect } from "react-redux"
import Balance from "../components/Balance"
import Section from "../components/primitives/Section"
import { getSelectedCoinTicker, getSelectedCoin } from "../selectors/coins"

class BalanceContainer extends React.Component {
  render() {
    return (
      <Section
        draggable
        id="balance"
        heading="Balances"
        onHide={this.props.onHide}
      >
        <Balance
          coin={this.props.coin}
          balance={this.props.balance}
          ticker={this.props.ticker}
        />
      </Section>
    )
  }
}

export default connect(state => {
  const coin = getSelectedCoin(state)
  return {
    balance: state.coin.balance,
    ticker: getSelectedCoinTicker(state),
    coin
  }
})(BalanceContainer)
