import React, { Component } from 'react';
import { AuthProvider, AuthConsumer } from './context/AuthContext';
import { TickerProvider } from './context/TickerContext';
import Whitelisting from './Whitelisting';
import Credentials from './Credentials';
import TickerSelector from './TickerSelector';
import './App.css';

//import SimpleTrade from './SimpleTrade';
//import { BUY, SELL } from './SimpleTrade';

export default class App extends Component {

  render() {
    return (
      <AuthProvider>

        <Whitelisting/>

        <AuthConsumer>{auth =>
          <div>
            <Credentials visible={auth.whitelisted} onChange={this.onReload} />
            <TickerProvider>
              <TickerSelector visible={auth.whitelisted && auth.valid} />
            </TickerProvider>
          </div>
        }</AuthConsumer>

      </AuthProvider>
    )
  }

  /*
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
        
        {traders}
      </div>
    )
  } */
}