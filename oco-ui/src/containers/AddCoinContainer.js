import React, { Component } from 'react';

import { connect } from 'react-redux';
import * as exchangesActions from '../store/exchanges/actions';
import * as coinsActions from '../store/coins/actions';

import { Icon, Button, Form, Dropdown, Modal } from 'semantic-ui-react'
import FixedModal from '../components/primitives/FixedModal';

class AddCoinContainer extends Component {

  state = {
    exchange: undefined,
    pair: undefined
  };

  componentDidMount() {
    this.props.dispatch(exchangesActions.fetchExchanges());
  }

  onChangeExchange = (e, data) => {
    this.setState({ exchange: data.value });
    this.props.dispatch(exchangesActions.fetchPairs(data.value));
  };

  onChangePair = (e, data) => {
    const pair = this.props.pairs.find(p => p.key === data.value);
    this.setState({ pair : pair });
  }

  onSubmit = (coinContainer) => {
    this.props.dispatch(coinsActions.add(this.state.pair));
    this.props.history.push('/coin/' + this.state.pair.key);
  };

  render() {

    const exchanges = this.props.exchanges;
    const pairs = this.props.pairs;
    const ready = !!this.state.pair;

    return (
      <FixedModal>
        <Modal.Header>
          <Icon name='bitcoin' />
          Add coin
        </Modal.Header>
        <Modal.Content>
        <Form onSubmit={this.onSubmit}>
          <Form.Field>
            <Dropdown
              placeholder='Select exchange'
              fluid
              selection
              loading={exchanges.length === 0}
              value={this.state.exchange}
              options={exchanges.map(exchange => ({key: exchange, text: exchange, value: exchange}))}
              onChange={this.onChangeExchange}
            />
          </Form.Field>
          <Form.Field>
            <Dropdown
              placeholder='Select pair'
              fluid
              search
              loading={pairs.length === 0 && this.state.exchange !== undefined}
              disabled={pairs.length === 0}
              selection
              options={pairs.map(pair => ({key: pair.key, text: pair.shortName, value: pair.key}))}
              onChange={this.onChangePair}
            />
          </Form.Field>
          <Button primary disabled={!ready}>Add</Button>
        </Form>
        </Modal.Content>
      </FixedModal>
    );
  }
}

function mapStateToProps(state) {
  return {
    exchanges: state.exchanges.exchanges,
    pairs: state.exchanges.pairs
  };
}

export default connect(mapStateToProps)(AddCoinContainer);