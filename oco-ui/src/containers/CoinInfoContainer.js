import React from "react"
import { connect } from "react-redux"
import CoinInfo from "../components/CoinInfo"
import * as tickerActions from "../store/ticker/actions"

class CoinInfoContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = { loading: true }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.coin.key !== this.props.coin.key) {
      if (this.props.coin) {
        this.props.dispatch(tickerActions.stopTicker(this.props.coin))
      }
      this.setState({ loading: true }, () => this.props.dispatch(tickerActions.startTicker(nextProps.coin)))
    } else {
      this.setState({ loading: false })
    }
  }

  componentWillMount() {
    if (this.props.coin) {
      this.props.dispatch(tickerActions.startTicker(this.props.coin))
    }
  }

  componentWillUnmount() {
    if (this.props.coin) {
      this.props.dispatch(tickerActions.stopTicker(this.props.coin))
    }
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
    ticker: state.ticker.ticker,
    updateFocusedField: state.focus.fn
  }
}

export default connect(mapStateToProps)(CoinInfoContainer)
