import React, { Component } from 'react';
import Whitelisting from './Whitelisting';
import Credentials from './Credentials';

export default class App extends Component {
  render() {
    return (
      <div>
        <Whitelisting />
        <Credentials />
      </div>
    );
  }
}