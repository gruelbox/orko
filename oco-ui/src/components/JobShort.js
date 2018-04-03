import React from 'react';
import { Icon } from 'semantic-ui-react';

import Href from './primitives/Href';
import Link from './primitives/Link';

const BUY = 'BUY';
const SELL = 'SELL';

export default class JobShort extends React.Component {

  describe = (job) => {

    const ticker = (t) => (t.exchange + ": " + t.base + "-" + t.counter);

    if (job.jobType === 'OneCancelsOther') {
      return (
        ticker(job.tickTrigger) + ": "
        + (job.high ? " > " + job.high.thresholdAsString + " then " + this.describe(job.high.job) : "")
        + (job.low ? " < " + job.low.thresholdAsString + " then " + this.describe(job.low.job) : "")
      );
    } else if (job.jobType === 'LimitOrderJob') {
      return (
        "On " + ticker(job.tickTrigger) + " " + job.direction + " " + job.bigDecimals.amount
      );
    } else if (job.jobType === 'SoftTrailingStop' && job.direction === SELL) {
      return (
        "When "
        + ticker(job.tickTrigger) 
        + "> " + job.bigDecimals.stopPrice + " then sell at " + job.bigDecimals.limitPrice
        + " trailing"
      );
    } else if (job.jobType === 'SoftTrailingStop' && job.direction === BUY) {
      return (
        "When "
        + ticker(job.tickTrigger) 
        + "< " + job.bigDecimals.stopPrice + " then buy at " + job.bigDecimals.limitPrice
        + " trailing"
      );
    } else if (job.jobType === 'Alert') {
      return (
        "Send alert"
      );
    } else {
      return (
        "Complex"
      );
    }
  }

  render() {
    return (
      <div>
        <Href onClick={this.props.onRemove}>
          <Icon name="close"/>
        </Href>
        <Link to={'/job/' + this.props.job.id}>
          {this.describe(this.props.job)}
        </Link>
      </div>
    );
  }
}