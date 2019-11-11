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
import React, { useState, useContext } from "react"
import Immutable from "seamless-immutable"
import Alert from "../components/Alert"
import { isValidNumber } from "modules/common/util/numberUtils"
import uuidv4 from "uuid/v4"
import { FrameworkContext } from "FrameworkContainer"
import { AlertJob, JobType, OcoJob, ServerContext, AlertLevel } from "modules/server"
import { ServerCoin } from "modules/market"

interface State {
  highPrice: string
  lowPrice: string
  message: string
  merge: any
}

const createJob = (state: State): OcoJob => {
  const highPriceValid = state.highPrice && isValidNumber(state.highPrice) && Number(state.highPrice) > 0
  const lowPriceValid = state.lowPrice && isValidNumber(state.lowPrice) && Number(state.lowPrice) > 0

  const tickTrigger: ServerCoin = {
    exchange: this.props.coin.exchange,
    counter: this.props.coin.counter,
    base: this.props.coin.base
  }

  return {
    jobType: JobType.OCO,
    id: uuidv4(),
    tickTrigger: tickTrigger,
    verbose: false,
    low: lowPriceValid
      ? {
          thresholdAsString: String(this.state.job.lowPrice),
          job: {
            jobType: JobType.ALERT,
            id: uuidv4(),
            notification: {
              message:
                "Price of " +
                this.props.coin.name +
                " dropped below " +
                this.state.job.lowPrice +
                (this.state.job.message !== "" ? ": " + this.state.job.message : ""),
              level: AlertLevel.ALERT
            }
          } as AlertJob
        }
      : null,
    high: highPriceValid
      ? {
          thresholdAsString: String(this.state.job.highPrice),
          job: {
            jobType: JobType.ALERT,
            id: uuidv4(),
            notification: {
              message:
                "Price of " +
                this.props.coin.name +
                " rose above " +
                this.state.job.highPrice +
                (this.state.job.message !== "" ? ": " + this.state.job.message : ""),
              level: AlertLevel.ALERT
            }
          } as AlertJob
        }
      : null
  }
}

const CreateAlertContainer: React.FC<any> = () => {
  const frameworkApi = useContext(FrameworkContext)
  const serverApi = useContext(ServerContext)

  const [state, setState] = useState<State>(
    Immutable({
      highPrice: "",
      lowPrice: "",
      message: ""
    })
  )

  const onFocus = (focusedProperty: string) => {
    frameworkApi.setLastFocusedFieldPopulater(value => {
      console.log("Set focus to" + focusedProperty)
      setState(old =>
        old.merge({
          [focusedProperty]: value
        })
      )
    })
  }

  const isValidNumber = (val: any) => !isNaN(val) && val !== "" && val > 0
  const highPriceValid = this.state.job.highPrice && isValidNumber(this.state.job.highPrice)
  const lowPriceValid = this.state.job.lowPrice && isValidNumber(this.state.job.lowPrice)

  return (
    <Alert
      job={this.state.job}
      onChange={setState}
      onFocus={onFocus}
      onSubmit={() => serverApi.submitJob(createJob(state))}
      highPriceValid={highPriceValid}
      lowPriceValid={lowPriceValid}
    />
  )
}

export default CreateAlertContainer
