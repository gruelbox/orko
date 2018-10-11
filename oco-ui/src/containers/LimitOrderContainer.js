import React from "react"
import { connect } from "react-redux"
import Immutable from "seamless-immutable"

import LimitOrder from "../components/LimitOrder"

import * as focusActions from "../store/focus/actions"
import * as jobActions from "../store/job/actions"
import * as coinActions from "../store/coin/actions"
import * as jobTypes from "../services/jobTypes"
import { isValidNumber } from "../util/numberUtils"

class LimitOrderContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      job: Immutable({
        limitPrice: "",
        amount: ""
      })
    }
  }

  onChange = job => {
    this.setState({
      job: job
    })
  }

  onFocus = focusedProperty => {
    this.props.dispatch(
      focusActions.setUpdateAction(value => {
        this.setState(prev => ({
          job: prev.job.merge({
            [focusedProperty]: value
          })
        }))
      })
    )
  }

  createJob = direction => {
    const tickTrigger = {
      exchange: this.props.coin.exchange,
      counter: this.props.coin.counter,
      base: this.props.coin.base
    }

    return {
      jobType: jobTypes.LIMIT_ORDER,
      tickTrigger: tickTrigger,
      bigDecimals: {
        amount: this.state.job.amount,
        limitPrice: this.state.job.limitPrice
      },
      direction: direction
    }
  }

  onSubmit = async direction => {
    this.props.dispatch(
      jobActions.submitJob(this.createJob(direction), this.onStatus)
    )
    this.setState(current => ({
      job: Immutable.merge(current.job, {
        amount: ""
      })
    }))
  }

  onStatus = update => {
    if (update.status === "SUCCESS") {
      this.props.dispatch(coinActions.addOrder(update.payload))
    }
  }

  render() {
    const limitPriceValid =
      this.state.job.limitPrice &&
      isValidNumber(this.state.job.limitPrice) &&
      this.state.job.limitPrice > 0
    const amountValid =
      this.state.job.amount &&
      isValidNumber(this.state.job.amount) &&
      this.state.job.amount > 0

    return (
      <LimitOrder
        job={this.state.job}
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
    auth: state.auth
  }
}

export default connect(mapStateToProps)(LimitOrderContainer)
