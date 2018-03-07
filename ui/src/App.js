import React, { Component } from 'react';

import './App.css';

import { connect } from 'react-redux';

import { checkWhitelist } from './redux/auth';
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
    this.props.checkWhitelist();
  }

  onReload() {
    console.log("App: reload");
    this.props.invalidateCache(["tickers", "balances"]);
    console.log("App: reloaded");
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
  invalidateCache: (cacheKeys) => invalidateCache(cacheKeys),
  checkWhitelist: checkWhitelist
};

export default connect(mapStateToProps, mapDispatchToProps) (App);