import React, { Component } from 'react';
import { Menu, Dropdown, Container } from 'semantic-ui-react'
import { Link } from 'react-router-dom';
import { Subscribe  } from 'unstated';
import CoinContainer from './context/CoinContainer';
import { AuthConsumer } from './context/AuthContext';


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
            <Menu stackable>
              <Container>
                <Dropdown item simple text='Active coins'>
                  <Dropdown.Menu>
                    {coins}
                  </Dropdown.Menu>
                </Dropdown>
                <Menu.Item><Link to='/addcoin'>Add coin</Link></Menu.Item>
                <Menu.Item><Link to='/jobs'>Active jobs</Link></Menu.Item>
                <Menu.Item>
                  <AuthConsumer>{auth => (
                    <a onClick={auth.signout}>Sign out</a>
                  )}</AuthConsumer>
                </Menu.Item>
              </Container>
            </Menu>
          );
        }}
      </Subscribe>
    );
  }
}