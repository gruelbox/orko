import React from "react"
import { connect } from "react-redux"
import Immutable from "seamless-immutable"

import StopTakeProfit from "../components/StopTakeProfit"

import * as focusActions from "../store/focus/actions"
import * as jobActions from "../store/job/actions"
import * as jobTypes from "../services/jobTypes"
import { isValidNumber } from "../util/numberUtils"

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
      jobType: jobTypes.OCO,
      tickTrigger: tickTrigger,
      low: this.state.job.lowPrice
        ? {
            thresholdAsString: this.state.job.lowPrice,
            job: {
              jobType: jobTypes.LIMIT_ORDER,
              direction: this.state.job.direction,
              track: this.state.job.track,
              tickTrigger: tickTrigger,
              bigDecimals: {
                amount: this.state.job.amount,
                limitPrice: this.state.job.limitPrice
              }
            }
          }
        : null,
      high: this.state.job.highPrice
        ? {
            thresholdAsString: this.state.job.highPrice,
            job: {
              jobType: jobTypes.LIMIT_ORDER,
              direction: this.state.job.direction,
              track: this.state.job.track,
              tickTrigger: tickTrigger,
              bigDecimals: {
                amount: this.state.job.amount,
                limitPrice: this.state.job.limitPrice
              }
            }
          }
        : null
    }
  }

  onSubmit = async () => {
    this.props.dispatch(jobActions.submitJob(this.createJob()))
  }

  render() {
    const limitPriceValid =
      this.state.job.limitPrice && isValidNumber(this.state.job.limitPrice) && this.state.job.limitPrice > 0
    const highPriceValid =
      this.state.job.highPrice && isValidNumber(this.state.job.highPrice) && this.state.job.highPrice > 0
    const lowPriceValid =
      this.state.job.lowPrice && isValidNumber(this.state.job.lowPrice) && this.state.job.lowPrice > 0
    const amountValid =
      this.state.job.amount && isValidNumber(this.state.job.amount) && this.state.job.amount > 0

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
