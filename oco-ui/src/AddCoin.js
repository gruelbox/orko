import React, { Component } from 'react';
import { Icon, Header, Button, Form, Dropdown, Segment } from 'semantic-ui-react'
import { Subscribe  } from 'unstated';
import ExchangeContainer from './context/ExchangeContainer';
import { AuthConsumer } from './context/AuthContext';
import PairContainer from './context/PairContainer';
import CoinContainer from './context/CoinContainer';

export default class AddCoin extends Component {

  state = {
    exchange: undefined,
    pair: undefined
  };

  onChangeExchange = (e, data, auth, pairContainer) => {
    pairContainer.getPairs(data.value, auth);
    this.setState({ exchange : data.value });
  };

  onChangePair = (e, data, auth, pairContainer) => {
    const pair = pairContainer.getPairs(this.state.exchange, auth).find(p => p.key === data.value);
    this.setState({ pair : pair });
  }

  onSubmit = (coinContainer) => {
    coinContainer.addCoin(this.state.pair);
    this.props.history.push('/coin/' + this.state.pair.key)
  };

  render() {
    return (
      <AuthConsumer>{auth => (
        <Subscribe to={[ExchangeContainer, PairContainer, CoinContainer]}>{(exchangeContainer, pairContainer, coinContainer) => {
          
          var exchanges = exchangeContainer.getExchanges(auth);
          if (!exchanges) exchanges = [];
          var pairs = pairContainer.getPairs(this.state.exchange, auth);
          if (!pairs) pairs = [];

          return (<Segment>
            <Header as='h2'>
              <Icon name='bitcoin' />
              Add coin
            </Header>
            <Form onSubmit={() => this.onSubmit(coinContainer)}>
              <Form.Field>
                <Dropdown
                  placeholder='Select exchange'
                  fluid
                  selection
                  loading={exchanges.length === 0}
                  value={this.state.exchange}
                  options={exchanges.map(exchange => ({key: exchange, text: exchange, value: exchange}))}
                  onChange={(e, d) => this.onChangeExchange(e, d, auth, pairContainer)}
                />
              </Form.Field>
              <Form.Field>
                <Dropdown
                  placeholder='Select pair'
                  fluid
                  search
                  loading={pairs.length === 0}
                  disabled={pairs.length === 0}
                  selection
                  options={pairs.map(pair => ({key: pair.key, text: pair.shortName, value: pair.key}))}
                  onChange={(e, d) => this.onChangePair(e, d, auth, pairContainer)}
                />
              </Form.Field>
              <Button primary disabled={!this.state.pair}>Add</Button>
            </Form>
          </Segment>);
        }}</Subscribe>
      )}</AuthConsumer>
    )
  }
}