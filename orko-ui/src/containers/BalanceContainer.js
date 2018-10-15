import React from "react"
import { connect } from "react-redux"
import Balance from "../components/Balance"
import Section from "../components/primitives/Section"
import { getSelectedCoinTicker } from "../selectors/coins"
import { areEqualShallow } from "../util/objectUtils"

class BalanceContainer extends React.Component {
  shouldComponentUpdate(nextProps) {
    if (this.props.balance && !nextProps.balance) {
      return true
    }
    if (!this.props.balance && nextProps.balance) {
      return true
    }
    if (
      this.props.balance &&
      nextProps.balance &&
      !areEqualShallow(this.props.balance, nextProps.balance)
    ) {
      return true
    }
    return false
  }

  render() {
    return (
      <Section id="balance" heading="Balances">
        <Balance
          coin={this.props.coin}
          balance={this.props.balance}
          ticker={this.props.ticker}
        />
      </Section>
    )
  }
}

export default connect(state => ({
  balance: state.coin.balance,
  ticker: getSelectedCoinTicker(state)
}))(BalanceContainer)
