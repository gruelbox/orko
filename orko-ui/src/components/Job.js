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
import React, { Component } from "react"
import { Icon } from "semantic-ui-react"

import * as jobTypes from "../services/jobTypes"
import JobStage from "./primitives/JobStage"
import JobStages from "./primitives/JobStages"

const BUY = "BUY"
const SELL = "SELL"

export default class Job extends Component {
  render() {
    const job = this.props.job
    if (job.jobType === jobTypes.OCO) {
      return (
        <JobStages>
          <JobStage>
            <Icon name="eye" /> Watch price{" "}
            {job.tickTrigger.base + "/" + job.tickTrigger.counter} on{" "}
            {job.tickTrigger.exchange}
            <JobStages>
              {job.high && (
                <JobStage>
                  <Icon name="pointing up" />
                  If price rises above {job.high.thresholdAsString}
                  <Job job={job.high.job} />
                </JobStage>
              )}
              {job.low && (
                <JobStage>
                  <Icon name="pointing down" />
                  If price drops below {job.low.thresholdAsString}
                  <Job job={job.low.job} />
                </JobStage>
              )}
            </JobStages>
          </JobStage>
        </JobStages>
      )
    } else if (job.jobType === jobTypes.LIMIT_ORDER) {
      return (
        <JobStages>
          <JobStage>
            <Icon name={job.direction === BUY ? "caret up" : "caret down"} />
            {job.direction} <b>{job.amount}</b> {job.tickTrigger.base} for{" "}
            {job.tickTrigger.counter} on <b>{job.tickTrigger.exchange}</b> at{" "}
            <b>{job.limitPrice}</b>
          </JobStage>
        </JobStages>
      )
    } else if (
      job.jobType === jobTypes.SOFT_TRAILING_STOP &&
      job.direction === SELL
    ) {
      return (
        <JobStages>
          <JobStage>
            <Icon name="eye" /> Watch price{" "}
            {job.tickTrigger.base + "/" + job.tickTrigger.counter} on{" "}
            {job.tickTrigger.exchange}
            <JobStages>
              <JobStage>
                <Icon name="pointing down" />
                If the bid price drops below {job.stopPrice}
                <JobStages>
                  <JobStage>
                    <Icon name="caret down" />
                    Sell at {job.limitPrice}
                  </JobStage>
                </JobStages>
              </JobStage>
              <JobStage>
                <Icon name="pointing up" />
                If the price rises
                <JobStages>
                  <JobStage>
                    <Icon name="caret up" />
                    Raise the stop price by the same amount, but not the limit
                    price. Highest bid price recorded so far is{" "}
                    {job.lastSyncPrice}
                  </JobStage>
                </JobStages>
              </JobStage>
            </JobStages>
          </JobStage>
        </JobStages>
      )
    } else if (
      job.jobType === jobTypes.SOFT_TRAILING_STOP &&
      job.direction === BUY
    ) {
      return (
        <JobStages>
          <JobStage>
            <Icon name="eye" /> Watch price{" "}
            {job.tickTrigger.base + "/" + job.tickTrigger.counter} on{" "}
            {job.tickTrigger.exchange}
            <JobStages>
              <JobStage>
                <Icon name="pointing up" />
                If ask price rises above {job.stopPrice}
                <JobStages>
                  <JobStage>
                    <Icon name="caret up" />
                    Buy at {job.limitPrice}
                  </JobStage>
                </JobStages>
              </JobStage>
              <JobStage>
                <Icon name="pointing down" />
                If the price drops
                <JobStages>
                  <JobStage>
                    <Icon name="caret down" />
                    Lower the stop price by the same amount, but not the limit
                    price. Lowest ask price recorded so far is{" "}
                    {job.lastSyncPrice}
                  </JobStage>
                </JobStages>
              </JobStage>
            </JobStages>
          </JobStage>
        </JobStages>
      )
    } else if (job.jobType === jobTypes.ALERT) {
      return (
        <JobStages>
          <JobStage>
            <Icon name="computer" />
            Send a telegram message: '{job.notification.message}'
          </JobStage>
        </JobStages>
      )
    } else if (job.jobType === jobTypes.STATUS_UPDATE) {
      return (
        <JobStages>
          <JobStage>
            <Icon name="computer" />
            Status update
          </JobStage>
        </JobStages>
      )
    } else if (job.jobType === jobTypes.SCRIPT) {
      return (
        <>
          <h3>{job.name}</h3>
          <pre>{job.script}</pre>
        </>
      )
    } else {
      return (
        <JobStages>
          <JobStage>
            <Icon name="computer" />
            {JSON.stringify(job, null, 2)}
          </JobStage>
        </JobStages>
      )
    }
  }
}
