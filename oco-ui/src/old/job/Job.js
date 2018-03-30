import React, { Component } from 'react';
import { List } from 'semantic-ui-react';
import { BUY, SELL } from '../context/trade';

export default class Job extends Component {
  render() {
    const job = this.props.job;
    if (job.jobType === 'OneCancelsOther') {
      return (
        <List.Item>
          <List.Icon name='eye' />
          <List.Content>
            <List.Header>Watch price</List.Header>
            <List.Description>{job.tickTrigger.base + "/" + job.tickTrigger.counter} on {job.tickTrigger.exchange}</List.Description>
            <List.List>
              { job.high &&
                <List.Item>
                  <List.Icon name='pointing up' />
                  <List.Content>
                    <List.Description>If price rises above {job.high.thresholdAsString}</List.Description>
                    <List.List>
                      <Job job={job.high.job} />
                    </List.List>
                  </List.Content>
                </List.Item>
              }
              { job.low &&
                <List.Item>
                  <List.Icon name='pointing down' />
                  <List.Content>
                    <List.Description>If price drops below {job.low.thresholdAsString}</List.Description>
                    <List.List>
                      <Job job={job.low.job} />
                    </List.List>
                  </List.Content>
                </List.Item>
              }
            </List.List>
          </List.Content>
        </List.Item>
      );
    } else if (job.jobType === 'LimitOrderJob') {
      return (
        <List.Item>
          <List.Icon name={job.direction === BUY ? 'caret up' : 'caret down'} />
          <List.Content>
            <List.Description>
              {job.direction} <b>{job.bigDecimals.amount}</b> {job.tickTrigger.base} for {job.tickTrigger.counter} on <b>{job.tickTrigger.exchange}</b> at <b>{job.bigDecimals.limitPrice}</b>
            </List.Description>
          </List.Content>
        </List.Item>
      );
    } else if (job.jobType === 'SoftTrailingStop' && job.direction === SELL) {
      return (
        <List.Item>
          <List.Icon name='eye' />
          <List.Content>
            <List.Header>Watch price</List.Header>
            <List.Description>{job.tickTrigger.base + "/" + job.tickTrigger.counter} on {job.tickTrigger.exchange}</List.Description>
            <List.List>
              <List.Item>
                <List.Icon name='pointing down' />
                <List.Content>
                  <List.Description>If the bid price drops below {job.bigDecimals.stopPrice}</List.Description>
                  <List.List>
                    <List.Item>
                      <List.Icon name='caret down' />
                      <List.Content>
                        <List.Description>Sell at {job.bigDecimals.limitPrice}</List.Description>
                      </List.Content>
                    </List.Item>
                  </List.List>
                </List.Content>
              </List.Item>
              <List.Item>
                <List.Icon name='pointing up' />
                <List.Content>
                  <List.Description>If the price rises</List.Description>
                  <List.List>
                    <List.Item>
                      <List.Icon name='caret up' />
                      <List.Content>
                        <List.Description>Raise the stop price by the same amount, but not the limit price</List.Description>
                      </List.Content>
                    </List.Item>
                  </List.List>
                </List.Content>
              </List.Item>
            </List.List>
          </List.Content>
        </List.Item>
      );
    } else if (job.jobType === 'SoftTrailingStop' && job.direction === BUY) {
      return (
        <List.Item>
          <List.Icon name='eye' />
          <List.Content>
            <List.Header>Watch price</List.Header>
            <List.Description>{job.tickTrigger.base + "/" + job.tickTrigger.counter} on {job.tickTrigger.exchange}</List.Description>
            <List.List>
              <List.Item>
                <List.Icon name='pointing up' />
                <List.Content>
                  <List.Description>If ask price rises above {job.bigDecimals.stopPrice}</List.Description>
                  <List.List>
                    <List.Item>
                      <List.Icon name='caret up' />
                      <List.Content>
                        <List.Description>Buy at {job.bigDecimals.limitPrice}</List.Description>
                      </List.Content>
                    </List.Item>
                  </List.List>
                </List.Content>
              </List.Item>
              <List.Item>
                <List.Icon name='pointing down' />
                <List.Content>
                  <List.Description>If the price drops</List.Description>
                  <List.List>
                    <List.Item>
                      <List.Icon name='caret down' />
                      <List.Content>
                        <List.Description>Lower the stop price by the same amount, but not the limit price</List.Description>
                      </List.Content>
                    </List.Item>
                  </List.List>
                </List.Content>
              </List.Item>
            </List.List>
          </List.Content>
        </List.Item>
      );
    } else if (job.jobType === 'Alert') {
      return (
        <List.Item>
          <List.Icon name='computer' />
          <List.Content>
            <List.Description><b>Alert</b>: '{job.message}'</List.Description>
          </List.Content>
        </List.Item>
      );
    } else {
      return (
        <List.Item>
          <List.Icon name='computer' />
          <List.Content>
            <List.Description>{JSON.stringify(job, null, 2)}</List.Description>
          </List.Content>
        </List.Item>
      );
    }
  }
}