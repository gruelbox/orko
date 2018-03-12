import React, { Component } from 'react';
import TradingViewWidget from 'react-tradingview-widget';

export default class App extends Component {

  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return false;
  }

  render() {
    return (
      <div style={{height: 500}}>
        <TradingViewWidget 
          symbol={this.props.symbol}
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