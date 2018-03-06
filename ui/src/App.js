import React, { Component } from 'react';

import './App.css';

import { connect } from 'react-redux';

import { fetchBalances } from './redux/balance';
import { fetchTicker } from './redux/ticker';
import { invalidateCache } from 'redux-cache';

import SimpleTrade from './SimpleTrade';
import { BUY, SELL } from './SimpleTrade';
import Credentials from './Credentials';
import Whitelisting from './Whitelisting';

class App extends Component {

  constructor(props) {
    super(props);
    this.onReload = this.onReload.bind(this);
  }

  componentDidMount() {
    //this.props.fetchBalances("binance", ["VEN", "BTC"]);
    //this.props.fetchTicker("binance", "BTC", "VEN");
  }

  onReload() {
    this.props.invalidateCache("balances");
    this.props.invalidateCache("tickers");
    this.props.fetchBalances("binance", ["VEN", "BTC"]);
    this.props.fetchTicker("binance", "BTC", "VEN");
  }

  render() {
    const traders = this.props.auth.valid
    ?<div>
      <SimpleTrade direction={BUY} exchange="binance" base="VEN" counter="BTC"/>
      <SimpleTrade direction={SELL} exchange="binance" base="VEN" counter="BTC"/>
    </div>
    : null;
    return (
      <div>
        <Whitelisting onChange={this.onReload} />
        <Credentials visible={this.props.auth.whitelisted} onChange={this.onReload} />
        {traders}
      </div>
    )
  }
}

const mapStateToProps = state => {
  return {
    auth: state.auth
  }
}

const mapDispatchToProps = {
  fetchBalances: (exchange, currencies) => fetchBalances(exchange, currencies),
  fetchTicker: (exchange, counter, base) => fetchTicker(exchange, counter, base),
	invalidateCache: (cacheKeys) => invalidateCache(cacheKeys)
};

export default connect(mapStateToProps, mapDispatchToProps) (App);