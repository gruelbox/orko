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
import { Icon } from "semantic-ui-react"

import JobStage from "./primitives/JobStage"
import JobStages from "./primitives/JobStages"
import {
  Job,
  JobType,
  OcoJob,
  LimitOrderJob,
  TradeDirection,
  SoftTrailingStopJob,
  AlertJob,
  StatusUpdateJob,
  ScriptJob
} from "modules/server"

export interface JobComponentProps {
  job: Job
}

const OcoJobComponent: React.FC<{ job: OcoJob }> = ({ job }) => (
  <JobStages>
    <JobStage>
      <Icon name="eye" /> Watch price {job.tickTrigger.base + "/" + job.tickTrigger.counter} on{" "}
      {job.tickTrigger.exchange}
      <JobStages>
        {job.high && (
          <JobStage>
            <Icon name="pointing up" />
            If price rises above {job.high.thresholdAsString}
            <JobComponent job={job.high.job} />
          </JobStage>
        )}
        {job.low && (
          <JobStage>
            <Icon name="pointing down" />
            If price drops below {job.low.thresholdAsString}
            <JobComponent job={job.low.job} />
          </JobStage>
        )}
      </JobStages>
    </JobStage>
  </JobStages>
)

const LimitOrderJobComponent: React.FC<{ job: LimitOrderJob }> = ({ job }) => (
  <JobStages>
    <JobStage>
      <Icon name={job.direction === TradeDirection.BUY ? "caret up" : "caret down"} />
      {job.direction} <b>{job.amount}</b> {job.tickTrigger.base} for {job.tickTrigger.counter} on{" "}
      <b>{job.tickTrigger.exchange}</b> at <b>{job.limitPrice}</b>
    </JobStage>
  </JobStages>
)

const SoftTrailingStopJobComponent: React.FC<{ job: SoftTrailingStopJob }> = ({ job }) => {
  if (job.direction === TradeDirection.SELL) {
    return (
      <JobStages>
        <JobStage>
          <Icon name="eye" /> Watch price {job.tickTrigger.base + "/" + job.tickTrigger.counter} on{" "}
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
                  Raise the stop price by the same amount, but not the limit price. Highest bid price recorded
                  so far is {job.lastSyncPrice}
                </JobStage>
              </JobStages>
            </JobStage>
          </JobStages>
        </JobStage>
      </JobStages>
    )
  } else {
    return (
      <JobStages>
        <JobStage>
          <Icon name="eye" /> Watch price {job.tickTrigger.base + "/" + job.tickTrigger.counter} on{" "}
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
                  Lower the stop price by the same amount, but not the limit price. Lowest ask price recorded
                  so far is {job.lastSyncPrice}
                </JobStage>
              </JobStages>
            </JobStage>
          </JobStages>
        </JobStage>
      </JobStages>
    )
  }
}

const AlertJobComponent: React.FC<{ job: AlertJob }> = ({ job }) => (
  <JobStages>
    <JobStage>
      <Icon name="computer" />
      Send a telegram message: '{job.notification.message}'
    </JobStage>
  </JobStages>
)

const StatusUpdateJobComponent: React.FC<{ job: StatusUpdateJob }> = ({ job }) => (
  <JobStages>
    <JobStage>
      <Icon name="computer" />
      Status update
    </JobStage>
  </JobStages>
)

const ScriptJobComponent: React.FC<{ job: ScriptJob }> = ({ job }) => (
  <>
    <h3>{job.name}</h3>
    <pre>{job.script}</pre>
  </>
)

const JobComponent: React.FC<JobComponentProps> = ({ job }) => {
  if (job.jobType === JobType.OCO) {
    return <OcoJobComponent job={job as OcoJob} />
  } else if (job.jobType === JobType.LIMIT_ORDER) {
    return <LimitOrderJobComponent job={job as LimitOrderJob} />
  } else if (job.jobType === JobType.SOFT_TRAILING_STOP) {
    return <SoftTrailingStopJobComponent job={job as SoftTrailingStopJob} />
  } else if (job.jobType === JobType.ALERT) {
    return <AlertJobComponent job={job as AlertJob} />
  } else if (job.jobType === JobType.STATUS_UPDATE) {
    return <StatusUpdateJobComponent job={job as StatusUpdateJob} />
  } else if (job.jobType === JobType.SCRIPT) {
    return <ScriptJobComponent job={job as ScriptJob} />
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

export default JobComponent
