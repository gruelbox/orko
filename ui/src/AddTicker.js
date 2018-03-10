import React, { Component } from 'react';
import { Icon, Header, Button, Form, Dropdown, Loader, Dimmer } from 'semantic-ui-react'
import { Subscribe  } from 'unstated';
import { ExchangeContainer } from './context/ExchangeContainer';
import { AuthContainer } from './context/AuthContainer';
import { PairContainer } from './context/PairContainer';
import { TickerContainer } from './context/TickerContainer';

export default class AddTicker extends Component {

  state = {
    exchanges: [],
    pairs: [],
    exchange: undefined,
    pair: undefined
  };

  onChangeExchange = (e, data, auth, pairContainer) => {
    if (data.value) {
      pairContainer.getPairs(data.value, auth)
        .then((pairs) => {
          this.setState({ pairs: pairs })
        });
    }
    this.setState({ exchange : data.value });
  }

  onChangePair = (e, data) => this.setState({ pair : data.value });

  onSubmit = (tickerContainer) => {
    tickerContainer.addTicker(this.state.pairs.find(p => p.key === this.state.pair));
    this.props.history.push('/ticker/' + this.state.pair)
  }

  render() {
    return (
      <Subscribe to={[ExchangeContainer, AuthContainer, PairContainer, TickerContainer]}>
        {(exchangeContainer, auth, pairContainer, tickerContainer) => {

          exchangeContainer.getExchanges(auth)
            .then((exchanges) => this.setState({ exchanges: exchanges }));

          return <div>
            <Dimmer active={this.state.exchanges.length === 0}>
              <Loader>
                Can't contact server.
              </Loader>
            </Dimmer>
            <Header as='h2'>
              <Icon name='bitcoin' />
              Add ticker
            </Header>
            <Form onSubmit={() => this.onSubmit(tickerContainer)}>
              <Form.Field>
                <Dropdown
                  placeholder='Select exchange'
                  fluid
                  selection
                  value={this.state.exchange}
                  options={this.state.exchanges.map(exchange => ({key: exchange, text: exchange, value: exchange}))}
                  onChange={(e, d) => this.onChangeExchange(e, d, auth, pairContainer)}
                />
              </Form.Field>
              <Form.Field>
                <Dropdown
                  placeholder='Select pair'
                  fluid
                  search
                  selection
                  options={this.state.pairs.map(pair => ({key: pair.key, text: pair.shortName, value: pair.key}))}
                  onChange={this.onChangePair}
                />
              </Form.Field>
              <Button primary disabled={!this.state.pair}>Add</Button>
            </Form>
          </div>
        }}
      </Subscribe>
    )
  }
}