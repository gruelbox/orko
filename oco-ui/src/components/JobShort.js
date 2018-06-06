import React from "react"
import { Icon } from "semantic-ui-react"
import styled from "styled-components"

import Href from "./primitives/Href"
import Link from "./primitives/Link"
import FlashEntry from "./primitives/FlashEntry"
import * as jobTypes from "../services/jobTypes"

const BUY = "BUY"
const SELL = "SELL"

const JobShortBox = styled.div`
  padding-top: ${props => props.theme.space[1] + "px"};
  padding-bottom: ${props => props.theme.space[1] + "px"};
  border-bottom: 1px solid ${props => props.theme.colors.deemphasis};
  &:last-child {
    border: none;
  }
`

export default class JobShort extends React.Component {
  describe = job => {
    const ticker = t => t.exchange + ": " + t.base + "-" + t.counter

    if (job.jobType === jobTypes.OCO) {
      return (
        "On " +
        ticker(job.tickTrigger) +
        ": " +
        (job.high
          ? " If > " +
            job.high.thresholdAsString +
            " then " +
            this.describe(job.high.job) +
            "."
          : "") +
        (job.low
          ? " If < " +
            job.low.thresholdAsString +
            " then " +
            this.describe(job.low.job) +
            "."
          : "")
      )
    } else if (job.jobType === jobTypes.LIMIT_ORDER) {
      return (
        "On " +
        ticker(job.tickTrigger) +
        " " +
        job.direction +
        " " +
        job.bigDecimals.amount
      )
    } else if (
      job.jobType === jobTypes.SOFT_TRAILING_STOP &&
      job.direction === SELL
    ) {
      return (
        "When " +
        ticker(job.tickTrigger) +
        "> " +
        job.bigDecimals.stopPrice +
        " then sell at " +
        job.bigDecimals.limitPrice +
        " trailing at " + job.bigDecimals.lastSyncPrice
      )
    } else if (
      job.jobType === jobTypes.SOFT_TRAILING_STOP &&
      job.direction === BUY
    ) {
      return (
        "When " +
        ticker(job.tickTrigger) +
        "< " +
        job.bigDecimals.stopPrice +
        " then buy at " +
        job.bigDecimals.limitPrice +
        " trailing at " + job.bigDecimals.lastSyncPrice
      )
    } else if (job.jobType === jobTypes.ALERT) {
      return "Send alert"
    } else if (job.jobType === jobTypes.WATCH_JOB) {
      return (
        "Watch order " +
        job.orderId +
        " on " +
        job.tickTrigger.base +
        "/" +
        job.tickTrigger.counter +
        " on " +
        job.tickTrigger.exchange
      )
    } else {
      return "Complex (" + job.jobType + ")"
    }
  }

  render() {
    return (
      <FlashEntry>
        <JobShortBox>
          <Href title="Delete job" onClick={this.props.onRemove}>
            <Icon name="close" />
          </Href>
          <Link to={"/job/" + this.props.job.id}>
            {this.describe(this.props.job)}
          </Link>
        </JobShortBox>
      </FlashEntry>
    )
  }
}
