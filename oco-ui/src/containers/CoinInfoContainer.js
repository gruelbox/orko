import React from "react"
import { connect } from "react-redux"
import CoinInfo from "../components/CoinInfo"
import * as coinActions from "../store/coin/actions"

const TICK_TIME = 5000

class CoinInfoContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = { loading: true }
  }

  tick = () => {
    this.props.dispatch(coinActions.fetchTicker(this.props.coin))
    this.props.dispatch(coinActions.fetchBalance(this.props.coin))
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.coin.key !== this.props.coin.key) {
      this.setState({ loading: true }, () => this.tick())
    } else {
      this.setState({ loading: false })
    }
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
      <CoinInfo
        coin={this.props.coin}
        balance={this.props.balance}
        ticker={this.props.ticker}
        loading={this.state.loading}
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
    ticker: state.coin.ticker,
    updateFocusedField: state.focus.fn
  }
}

export default connect(mapStateToProps)(CoinInfoContainer)
