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
import styled from "styled-components"

import Href from "./primitives/Href"
import Link from "./primitives/Link"
import {
  Job,
  JobType,
  OcoJob,
  SoftTrailingStopJob,
  TradeDirection,
  LimitOrderJob,
  AlertJob,
  ScriptJob
} from "modules/server"
import { ServerCoin } from "modules/market"

const JobShortBox = styled.div`
  padding-top: ${props => props.theme.space[1] + "px"};
  padding-bottom: ${props => props.theme.space[1] + "px"};
  border-bottom: 1px solid ${props => props.theme.colors.deemphasis};
  &:last-child {
    border: none;
  }
`

const ticker = (t: ServerCoin) => t.exchange + ": " + t.base + "-" + t.counter

const ocoJob = (job: OcoJob) =>
  "On " +
  ticker(job.tickTrigger) +
  ": " +
  (job.high ? " If > " + job.high.thresholdAsString + " then " + describe(job.high.job) + "." : "") +
  (job.low ? " If < " + job.low.thresholdAsString + " then " + describe(job.low.job) + "." : "")

const limitOrderJob = (job: LimitOrderJob) =>
  "On " + ticker(job.tickTrigger) + " " + job.direction + " " + job.amount

const softTrailingStopJob = (job: SoftTrailingStopJob) => {
  if (job.direction === TradeDirection.BUY) {
    return (
      "When " +
      ticker(job.tickTrigger) +
      " > " +
      job.stopPrice +
      " then buy at " +
      job.limitPrice +
      ". Lowest ask price recorded is " +
      job.lastSyncPrice
    )
  } else {
    return (
      "When " +
      ticker(job.tickTrigger) +
      " < " +
      job.stopPrice +
      " then sell at " +
      job.limitPrice +
      ". Highest bid price recorded is " +
      job.lastSyncPrice
    )
  }
}

const describe = (job: Job) => {
  if (job.jobType === JobType.OCO) {
    return ocoJob(job as OcoJob)
  } else if (job.jobType === JobType.LIMIT_ORDER) {
    return limitOrderJob(job as LimitOrderJob)
  } else if (job.jobType === JobType.SOFT_TRAILING_STOP) {
    return softTrailingStopJob(job as SoftTrailingStopJob)
  } else if (job.jobType === JobType.ALERT) {
    return "Send alert '" + (job as AlertJob).notification.message + "'"
  } else if (job.jobType === JobType.STATUS_UPDATE) {
    return "Status update"
  } else if (job.jobType === JobType.SCRIPT) {
    return (job as ScriptJob).name
  } else {
    return "Complex (" + job.jobType + ")"
  }
}

export interface JobShortComponentProps {
  job: Job
  onRemove(): void
}

const JobShortComponent: React.FC<JobShortComponentProps> = (props) => (
  <JobShortBox data-orko={"job/" + props.job.id}>
    <Href title="Delete job" onClick={props.onRemove}>
      <Icon name="close" />
    </Href>
    <Link to={"/job/" + props.job.id}>{describe(props.job)}</Link>
  </JobShortBox>
)

export default JobShortComponent
