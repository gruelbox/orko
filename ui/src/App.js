import React, { Component } from 'react';
import AppBar from 'material-ui/AppBar';
import TickerSelector from './TickerSelector'
import TradingViewWidget from 'react-tradingview-widget';
import './App.css';

export default class App extends Component {
  render() {
    return (
      <div>
        {/* <AppBar
          title="Background Trade Control"
          iconClassNameRight="muidocs-icon-navigation-expand-more"
        /> */ }
        {/* <TradingViewWidget symbol="VENBTC" autosize /> */}
        <TickerSelector/>
      </div>
    );
  }
}