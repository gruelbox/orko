import React, { Component } from 'react';
import TradingViewWidget from 'react-tradingview-widget';

export default class App extends Component {

  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return this.props.coin.key !== nextProps.coin.key;
  }

  render() {
    return (
      <div style={{height: 500}}>
        <TradingViewWidget 
          symbol={this.props.coin.exchange.toUpperCase() +
                  ":" +
                  this.props.coin.base + this.props.coin.counter}
          hide_side_toolbar={false}
          autosize
          interval="240"
          allow_symbol_change={false}
          studies={['RSI@tv-basicstudies']}
        />
      </div>
    );
  }
}