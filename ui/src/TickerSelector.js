import React, { Component } from 'react';
import { Icon, Input, Header, Button, Form, Segment, Dropdown } from 'semantic-ui-react'
import './App.css';
import PropTypes from 'prop-types';
import { TickerConsumer } from './context/TickerContext';

export default class TickerSelector extends Component {

  constructor(props) {
    super(props);
    this.state = {
      exchanges: []
    };
  }

  render() {
    if (!this.props.visible)
      return null;
    return (
      <TickerConsumer>{tickerContext =>
        <Segment>
          <Header as='h2'>
            <Icon name='bitcoin' />
            Select ticker
          </Header>
          <Dropdown placeholder='Select exchange' fluid selection options={
            tickerContext.exchanges.map(e => ({key: e, text: e}))
          } />
        </Segment>
      }</TickerConsumer>
    )
  }
}

TickerSelector.propTypes = {
  visible: PropTypes.bool,
};

TickerSelector.defaultProps = {
  visible: true,
};