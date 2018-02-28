import React, { Component } from 'react';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import 'whatwg-fetch';

import './App.css';

export default class TickerSelector extends Component {

  constructor(props) {
    super(props);
    this.state = { success: false, values: [] };
    fetch(new Request(this.props.endPoint, {
      method: 'GET', 
      mode: 'cors', 
      redirect: 'follow',
      credentials: 'include',
      headers: new Headers({
        "Authorization": "Basic YnVsbHk6Ym95",
        "Content-type": "application/json"
      })
    }))
    .then(response => response.json())
    .then(json => {
      this.setState({ success: true, values: json});
    })
    .catch(function(err) {
      console.log('Fetch Error :-S', err);
    })
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