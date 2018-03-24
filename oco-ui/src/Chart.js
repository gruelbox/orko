import React, { Component } from 'react';
import TradingViewWidget from 'react-tradingview-widget';

export default class App extends Component {

  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return this.props.coin.key !== nextProps.coin.key;
  }

  render() {

    // Sad but true
    var exchange = this.props.coin.exchange.toUpperCase();
    if (exchange === 'GDAX') {
      exchange = "COINBASE";
    }

    return (
      <div style={{height: 500}}>
        <TradingViewWidget 
          symbol={exchange + ":" + this.props.coin.base + this.props.coin.counter}
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