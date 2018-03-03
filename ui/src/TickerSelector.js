import React, { Component } from 'react';
import TickerPropertySelector from './TickerPropertySelector';
import TextField from 'material-ui/TextField';
import fetchData from './fetchData';
import './App.css';

export default class TickerSelector extends Component {

  constructor(props) {
    super(props);
    this.state = { balances : null };
    fetchData('exchanges/gdax/availablebalance/EUR', json => {
      this.setState({ counterbalance: json });
    });
    fetchData('exchanges/gdax/availablebalance/BTC', json => {
      this.setState({ basebalance: json });
    });
  }

  render() {
    return (
      <div>
        <TickerPropertySelector title="Exchange" value="gdax" endPoint="exchanges"/>
        <br/>
        <TickerPropertySelector title="Counter" value="EUR" endPoint="exchanges/gdax/counters"/>
        <TextField hintText="Available" floatingLabelText="Available" value={this.state.counterbalance ? this.state.counterbalance : 0} disabled={true} />
        <br/>
        <TickerPropertySelector title="Base" value="BTC" endPoint="exchanges/gdax/counters/EUR/bases"/>
        <TextField hintText="Available" floatingLabelText="Available" value={this.state.basebalance ? this.state.basebalance : 0} disabled={true} />
      </div>
    );
  }
}
