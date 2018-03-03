import React, { Component } from 'react';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import 'whatwg-fetch';
import fetchData from './fetchData';

import './App.css';

export default class TickerSelector extends Component {

  constructor(props) {
    super(props);
    this.state = { success: false, values: [] };
    fetchData(this.props.endPoint, json => {
      this.setState({ success: true, values: json});
    });
  }

  render() {
    return (
      <SelectField floatingLabelText={this.props.title} value={this.props.value} onChange={this.props.onChange} disabled={!this.state.success}>
        {this.state.values.map((value, index) => (
          <MenuItem value={value} primaryText={value} />
        ))}
      </SelectField>
    );
  }
}