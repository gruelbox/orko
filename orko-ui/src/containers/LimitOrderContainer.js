import React from "react"
import { connect } from "react-redux"
import Immutable from "seamless-immutable"

import LimitOrder from "../components/LimitOrder"

import * as focusActions from "../store/focus/actions"
import * as exchangesActions from "../store/exchanges/actions"
import { isValidNumber } from "../util/numberUtils"
import { getSelectedCoin } from "../selectors/coins"

class LimitOrderContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      order: Immutable({
        limitPrice: "",
        amount: ""
      })
    }
  }

  onChange = order => {
    this.setState({
      order
    })
  }

  onFocus = focusedProperty => {
    this.props.dispatch(
      focusActions.setUpdateAction(value => {
        this.setState(prev => ({
          order: prev.order.merge({
            [focusedProperty]: value
          })
        }))
      })
    )
  }

  createOrder = direction => ({
    type: direction === "BUY" ? "BID" : "ASK",
    counter: this.props.coin.counter,
    base: this.props.coin.base,
    amount: this.state.order.amount,
    limitPrice: this.state.order.limitPrice
  })

  onSubmit = async direction => {
    const order = this.createOrder(direction)
    this.props.dispatch(
      exchangesActions.submitOrder(this.props.coin.exchange, order)
    )
  }

  render() {
    const limitPriceValid =
      this.state.order.limitPrice &&
      isValidNumber(this.state.order.limitPrice) &&
      this.state.order.limitPrice > 0
    const amountValid =
      this.state.order.amount &&
      isValidNumber(this.state.order.amount) &&
      this.state.order.amount > 0

    return (
      <LimitOrder
        order={this.state.order}
        onChange={this.onChange}
        onFocus={this.onFocus}
        onBuy={() => this.onSubmit("BUY")}
        onSell={() => this.onSubmit("SELL")}
        limitPriceValid={limitPriceValid}
        amountValid={amountValid}
      />
    )
  }
}

function mapStateToProps(state) {
  return {
    auth: state.auth,
    coin: getSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(LimitOrderContainer)
