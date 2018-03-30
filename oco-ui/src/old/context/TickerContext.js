import React, { Component } from 'react';
import { get } from './fetchUtil';
import createReactContext from 'create-react-context';
import { AuthConsumer } from './AuthContext';
import PropTypes from 'prop-types';

const DEFAULT_TICKER = { bid: 0, ask: 0 };

// TODO hacky....
const TICK_TIME = 5000;

const TickerContext = createReactContext('ticker');

export const TickerConsumer = TickerContext.Consumer;

export class TickerProvider extends Component {  

  constructor() {
    super();
    this.state = { };
  }

  getTicker = (auth) => {
    const result = this.state.ticker;
    if (result) {
      return result;
    } else {
      if (!this.interval) {
        this.tick(auth);
        this.start(auth);
      }
      return DEFAULT_TICKER;
    }
  };

  start = (auth) => {
    this.interval = setInterval(() => this.tick(auth), TICK_TIME);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  render() {
    return (
      <AuthConsumer>{(auth) => (
        <TickerContext.Provider value={this.getTicker(auth)}>
          {this.props.children}
        </TickerContext.Provider>
      )}</AuthConsumer>      
    );
  }
  
  tick = (auth) => {

    if (!auth.valid)
      return;

    const coin = this.props.coin;

    const res = 'exchanges/' + coin.exchange + "/markets/" + coin.base + "-" + coin.counter + "/ticker";
    return get(res, auth.userName, auth.password)
      .then(auth.parseToJson)
      .catch(error => {
        console.log("Failed to fetch ticker", coin);
        this.setState({ ticker: undefined });
      }).then(ticker => {
        if (!ticker)
          return;
        this.setState({ ticker: ticker });
      });
  };
}

TickerProvider.propTypes = {
  coin: PropTypes.object.isRequired
};