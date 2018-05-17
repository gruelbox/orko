import React from "react"
import { connect } from "react-redux"

import TradeSelector from "../components/TradeSelector"
import * as coinActions from "../store/coin/actions"

const TICK_TIME = 5000;

class TradingContainer extends React.Component {

  tick = () => {
    this.props.dispatch(coinActions.fetchBalance(this.props.coin));
  }

  componentDidMount() {
    this.tick();
    this.interval = setInterval(this.tick, TICK_TIME);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  render() {
    return (
      <TradeSelector
        coin={this.props.coin}
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
    updateFocusedField: state.focus.fn
  }
}

export default connect(mapStateToProps)(TradingContainer)
