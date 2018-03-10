import React, { Component } from 'react';
import { Menu, Dropdown, Container } from 'semantic-ui-react'
import { Link } from 'react-router-dom';
import { Subscribe  } from 'unstated';
import { TickerContainer } from './context/TickerContainer';

export default class App extends Component {
  render() {
    return (
      <Subscribe to={[TickerContainer]}>
        { tickerContainer => {

          var tickers = [];
          tickerContainer.getTickers().forEach(ticker => tickers.push(
            <Dropdown.Item key={ticker.key}><Link to={ '/ticker/' + ticker.key }>{ticker.name}</Link></Dropdown.Item>       
          ));

          return (
            <Menu fixed='top'>
              <Container>
                <Dropdown item simple text='Tickers'>
                  <Dropdown.Menu>
                    {tickers}
                  </Dropdown.Menu>
                </Dropdown>
                <Menu.Item><Link to='/addticker'>Add ticker</Link></Menu.Item>
              </Container>
              <Menu.Menu position='right'>
                <Menu.Item><Link to='/'>Account</Link></Menu.Item>
              </Menu.Menu>
            </Menu>
          );
        }}
      </Subscribe>
    );
  }
}