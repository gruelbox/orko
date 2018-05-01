import React from "react"
import { connect } from "react-redux"
import Immutable from "seamless-immutable"

import StopTakeProfit from "../components/StopTakeProfit"

import * as focusActions from "../store/focus/actions"
import * as jobActions from "../store/job/actions"

class StopTakeProfitContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      job: Immutable({
        lowPrice: "",
        highPrice: "",
        limitPrice: "",
        amount: "",
        direction: "BUY",
        track: true
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
        console.log("Set focus to" + focusedProperty)
        this.setState(prev => ({
          job: prev.job.merge({
            [focusedProperty]: value
          })
        }))
      })
    )
  }

  createJob = () => {
    const tickTrigger = {
      exchange: this.props.coin.exchange,
      counter: this.props.coin.counter,
      base: this.props.coin.base
    }
    return {
      jobType: "OneCancelsOther",
      tickTrigger: tickTrigger,
      low: this.state.job.lowPrice ? {
        thresholdAsString: this.state.job.lowPrice,
        job: {
          jobType: "LimitOrderJob",
          direction: this.state.job.direction,
          track: this.state.job.track,
          tickTrigger: tickTrigger,
          bigDecimals: {
            amount: this.state.job.amount,
            limitPrice: this.state.job.limitPrice
          }
        }
      } : null,
      high: this.state.job.highPrice ? {
        thresholdAsString: this.state.job.highPrice,
        job: {
          jobType: "LimitOrderJob",
          direction: this.state.job.direction,
          track: this.state.job.track,
          tickTrigger: tickTrigger,
          bigDecimals: {
            amount: this.state.job.amount,
            limitPrice: this.state.job.limitPrice
          }
        }
      } : null
    }
  }

  onSubmit = async () => {
    this.props.dispatch(jobActions.submitJob(this.createJob()))
  }

  render() {
    const isValidNumber = val => !isNaN(val) && val !== "" && val > 0
    const limitPriceValid =
      this.state.job.limitPrice && isValidNumber(this.state.job.limitPrice)
    const highPriceValid =
      this.state.job.highPrice && isValidNumber(this.state.job.highPrice)
    const lowPriceValid =
      this.state.job.lowPrice && isValidNumber(this.state.job.lowPrice)
    const amountValid =
      this.state.job.amount && isValidNumber(this.state.job.amount)

    return (
      <StopTakeProfit
        job={this.state.job}
        onChange={this.onChange}
        onFocus={this.onFocus}
        onSubmit={this.onSubmit}
        limitPriceValid={limitPriceValid}
        highPriceValid={highPriceValid}
        lowPriceValid={lowPriceValid}
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

export default connect(mapStateToProps)(StopTakeProfitContainer)
