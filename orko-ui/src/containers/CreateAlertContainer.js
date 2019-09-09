/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React from "react"
import { connect } from "react-redux"

import Immutable from "seamless-immutable"
import Alert from "../components/Alert"

import * as focusActions from "../store/focus/actions"
import * as jobActions from "../store/job/actions"
import * as jobTypes from "../services/jobTypes"
import { isValidNumber } from "@orko-js-common/util/numberUtils"

import uuidv4 from "uuid/v4"

class CreateAlertContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      job: Immutable({
        highPrice: "",
        lowPrice: "",
        message: ""
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
    const highPriceValid =
      this.state.job.highPrice &&
      isValidNumber(this.state.job.highPrice) &&
      this.state.job.highPrice > 0
    const lowPriceValid =
      this.state.job.lowPrice &&
      isValidNumber(this.state.job.lowPrice) &&
      this.state.job.lowPrice > 0

    const tickTrigger = {
      exchange: this.props.coin.exchange,
      counter: this.props.coin.counter,
      base: this.props.coin.base
    }

    return {
      jobType: jobTypes.OCO,
      id: uuidv4(),
      tickTrigger: tickTrigger,
      verbose: false,
      low: lowPriceValid
        ? {
            thresholdAsString: String(this.state.job.lowPrice),
            job: {
              jobType: jobTypes.ALERT,
              id: uuidv4(),
              notification: {
                message:
                  "Price of " +
                  this.props.coin.name +
                  " dropped below " +
                  this.state.job.lowPrice +
                  (this.state.job.message !== ""
                    ? ": " + this.state.job.message
                    : ""),
                level: "ALERT"
              }
            }
          }
        : null,
      high: highPriceValid
        ? {
            thresholdAsString: String(this.state.job.highPrice),
            job: {
              jobType: "Alert",
              id: uuidv4(),
              notification: {
                message:
                  "Price of " +
                  this.props.coin.name +
                  " rose above " +
                  this.state.job.highPrice +
                  (this.state.job.message !== ""
                    ? ": " + this.state.job.message
                    : ""),
                level: "ALERT"
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
    const isValidNumber = val => !isNaN(val) && val !== "" && val > 0
    const highPriceValid =
      this.state.job.highPrice && isValidNumber(this.state.job.highPrice)
    const lowPriceValid =
      this.state.job.lowPrice && isValidNumber(this.state.job.lowPrice)

    return (
      <Alert
        job={this.state.job}
        onChange={this.onChange}
        onFocus={this.onFocus}
        onSubmit={this.onSubmit}
        highPriceValid={highPriceValid}
        lowPriceValid={lowPriceValid}
      />
    )
  }
}

function mapStateToProps(state) {
  return {
    auth: state.auth
  }
}

export default connect(mapStateToProps)(CreateAlertContainer)
