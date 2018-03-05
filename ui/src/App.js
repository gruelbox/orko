import React, { Component } from 'react';
import { Segment } from 'semantic-ui-react'
import SimpleTrade from './SimpleTrade';
import { BUY, SELL } from './SimpleTrade';
import './App.css';
import { fetchBalances } from './redux/balance';
import { fetchTicker } from './redux/ticker';
import { connect } from 'react-redux';
import { invalidateCache } from 'redux-cache';

class App extends Component {

  componentDidMount() {
    this.props.fetchBalances("binance", ["VEN", "BTC"]);
    this.props.fetchTicker("binance", "BTC", "VEN");
  }

  render() {
    return (
      <Segment.Group horizontal>
        <SimpleTrade direction={BUY} exchange="binance" base="VEN" counter="BTC"/>
        <SimpleTrade direction={SELL} exchange="binance" base="VEN" counter="BTC"/>
      </Segment.Group>
    )
  }
}

const mapStateToProps = state => {
  return { }
}

const mapDispatchToProps = {
  fetchBalances: (exchange, currencies) => fetchBalances(exchange, currencies),
  fetchTicker: (exchange, counter, base) => fetchTicker(exchange, counter, base),
	invalidateCache: (cacheKeys) => invalidateCache(cacheKeys)
};

export default connect(mapStateToProps, mapDispatchToProps) (App);