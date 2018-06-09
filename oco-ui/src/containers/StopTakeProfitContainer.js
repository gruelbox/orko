import React from "react"
import { connect } from "react-redux"
import Immutable from "seamless-immutable"

import StopTakeProfit from "../components/StopTakeProfit"

import * as focusActions from "../store/focus/actions"
import * as jobActions from "../store/job/actions"
import * as jobTypes from "../services/jobTypes"

class StopTakeProfitContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      job: Immutable({
        lowPrice: "",
        lowLimitPrice: "",
        lowTrailing: false,
        highPrice: "",
        highLimitPrice: "",
        highTrailing: false,
        initialTrailingStop: "",
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

    const limitOrder = (limitPrice) => ({
      jobType: jobTypes.LIMIT_ORDER,
      direction: this.state.job.direction,
      track: this.state.job.track,
      tickTrigger,
      bigDecimals: {
        amount: this.state.job.amount,
        limitPrice
      }
    })

    const trailingOrder = (startPrice, stopPrice, limitPrice) => ({
      jobType: jobTypes.SOFT_TRAILING_STOP,
      direction: this.state.job.direction,
      tickTrigger,
      bigDecimals: {
        amount: this.state.job.amount,
        startPrice,
        lastSyncPrice: startPrice,
        stopPrice,
        limitPrice
      }
    })

    return {
      jobType: jobTypes.OCO,
      tickTrigger: tickTrigger,
      low: this.state.job.lowPrice
        ? {
            thresholdAsString: this.state.job.lowPrice,
            job: this.state.job.lowTrailing
              ? trailingOrder(this.state.job.lowPrice, this.state.job.initialTrailingStop, this.state.job.lowLimitPrice)
              : limitOrder(this.state.job.lowLimitPrice)
          }
        : null,
      high: this.state.job.highPrice
        ? {
            thresholdAsString: this.state.job.highPrice,
            job: this.state.job.highTrailing
              ? trailingOrder(this.state.job.highPrice, this.state.job.initialTrailingStop, this.state.job.highLimitPrice)
              : limitOrder(this.state.job.highLimitPrice)
          }
        : null
    }
  }

  onSubmit = async () => {
    this.props.dispatch(jobActions.submitJob(this.createJob()))
  }

  render() {
    return (
      <StopTakeProfit
        job={this.state.job}
        onChange={this.onChange}
        onFocus={this.onFocus}
        onSubmit={this.onSubmit}
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
