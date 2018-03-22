import React, { Component } from 'react';
import { Menu, Dropdown, Container } from 'semantic-ui-react'
import { Link } from 'react-router-dom';
import { Subscribe  } from 'unstated';
import CoinContainer from './context/CoinContainer';

export default class App extends Component {
  render() {
    return (
      <Subscribe to={[CoinContainer]}>
        { coinContainer => {

          var coins = [];
          coinContainer.getCoins().forEach(coin => coins.push(
            <Dropdown.Item key={coin.key}><Link to={ '/coin/' + coin.key }>{coin.name}</Link></Dropdown.Item>       
          ));

          return (
            <Menu fixed='top'>
              <Container>
                <Dropdown item simple text='Active coins'>
                  <Dropdown.Menu>
                    {coins}
                  </Dropdown.Menu>
                </Dropdown>
                <Menu.Item><Link to='/addcoin'>Add coin</Link></Menu.Item>
                <Menu.Item><Link to='/jobs'>Active jobs</Link></Menu.Item>
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