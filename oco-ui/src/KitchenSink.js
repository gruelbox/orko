import React, { Component } from 'react';
import AlertContainer from './containers/AlertContainer';
import LimitOrderContainer from './containers/LimitOrderContainer';
import CoinInfoContainer from './containers/CoinInfoContainer';
import { Form, Header, Segment } from 'semantic-ui-react'

export default class KitchenSink extends Component {
  render() {
    return (
      <div>
        <Segment attached>
          <CoinInfoContainer />
        </Segment>
        <Header as='h2' attached='top'>Alert</Header>
        <Segment attached>
          <Form>
            <AlertContainer />
          </Form>
        </Segment>
        <Header as='h2' attached='top'>Limit</Header>
        <Segment attached>
          <Form>
            <LimitOrderContainer/>
          </Form>
        </Segment>
      </div>
    );
  }
}