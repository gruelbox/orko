import React, { Component } from 'react';
import {Step } from 'semantic-ui-react'

export default class JobStatus extends Component {
  render() {
    return (
      <Step.Group ordered size="tiny" attached='bottom'>
        <Step active completed={this.props.valid}>
          <Step.Content>
            <Step.Title>Preparing</Step.Title>
            <Step.Description>Entering details</Step.Description>
          </Step.Content>
        </Step>
        <Step active completed={this.props.processing || this.props.executedTrade || this.props.error}>
          <Step.Content>
            <Step.Title>Requested</Step.Title>
            <Step.Description>Submitted to server</Step.Description>
          </Step.Content>
        </Step>
        <Step active completed={this.props.executedTrade !== undefined}>
          <Step.Content>
            <Step.Title>Processing</Step.Title>
            <Step.Description>Server accepted request</Step.Description>
          </Step.Content>
        </Step>
      </Step.Group>
    );
  }
}