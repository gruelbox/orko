import React from "react"
import { connect } from "react-redux"
import Balance from "../components/Balance"

import * as coinActions from "../store/coin/actions"
import { getSelectedCoinTicker } from "../selectors/coins"
import { areEqualShallow } from "../util/objectUtils"

const TICK_TIME = 5000

class BalanceContainer extends React.Component {
  shouldComponentUpdate(nextProps) {
    if (this.props.balance && !nextProps.balance) {
      return true
    }
    if (!this.props.balance && nextProps.balance) {
      return true
    }
    if (this.props.balance &&
      nextProps.balance &&
      !areEqualShallow(this.props.balance, nextProps.balance)) {
      return true
    }
    return false
  }

  tick = () => {
    this.props.dispatch(coinActions.fetchBalance(this.props.coin))
  }

  componentDidMount() {
    this.tick()
    this.interval = setInterval(this.tick, TICK_TIME)
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  render() {
    return (
      <Balance
        coin={this.props.coin}
        balance={this.props.balance}
        ticker={this.props.ticker}
      />
    )
  }
}

export default connect(state => ({
  balance: state.coin.balance,
  ticker: getSelectedCoinTicker(state)
}))(BalanceContainer)
