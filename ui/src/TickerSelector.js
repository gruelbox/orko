import React, { Component } from 'react';
import TickerPropertySelector from './TickerPropertySelector';

import './App.css';

export default class TickerSelector extends Component {
  render() {
    return (
      <div>
        <TickerPropertySelector title="Exchange" value="binance" endPoint="http://localhost:8080/api/exchanges"/>
        <br/>
        <TickerPropertySelector title="Counter" value="USDT" endPoint="http://localhost:8080/api/exchanges/binance/counters"/>
        <br/>
        <TickerPropertySelector title="Base" value="BTC" endPoint="http://localhost:8080/api/exchanges/binance/counters/USDT/bases"/>
      </div>
    );
  }
}