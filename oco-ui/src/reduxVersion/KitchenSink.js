import React, { Component } from 'react';
import AlertContainer from './containers/AlertContainer';
import LimitOrderComponent from './components/LimitOrderComponent';
import CoinInfoContainer from './containers/CoinInfoContainer';

import Immutable from 'seamless-immutable';

import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import { Form, Header, Segment } from 'semantic-ui-react'

import * as reducers from './store/reducers';

const store = createStore(combineReducers(reducers), applyMiddleware(thunk));

export default class KitchenSink extends Component {

  constructor(props) {
    super(props);
    this.state = {
      limitOrderSell: Immutable({
        price: "200",
        amount: "100",
        direction: "SELL"
      }),
      limitOrderBuy: Immutable({
        price: "200",
        amount: "100",
        direction: "BUY"
      }),
    };
  }

  render() {

    const applyState = (prop, value) => this.setState({ [prop]: value });

    return (
      <Provider store={store}>
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
          <Header as='h2' attached='top'>Limit sell</Header>
          <Segment attached>
            <Form>
            < LimitOrderComponent job={this.state.limitOrderSell} onChange={job => applyState("limitOrderSell", job)}/>
            </Form>
          </Segment>
          <Header as='h2' attached='top'>Limit Buy</Header>
          <Segment attached>
            <Form>
              <LimitOrderComponent job={this.state.limitOrderBuy} onChange={job => applyState("limitOrderBuy", job)}/>
            </Form>
          </Segment>
        </div>
      </Provider>
    );
  }
}