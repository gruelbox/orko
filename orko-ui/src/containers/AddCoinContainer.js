/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React, { Component } from "react"

import { connect } from "react-redux"
import * as exchangesActions from "../store/exchanges/actions"
import * as coinsActions from "../store/coins/actions"

import { Icon, Button, Form, Dropdown, Modal } from "semantic-ui-react"
import FixedModal from "../components/primitives/FixedModal"

class AddCoinContainer extends Component {
  state = {
    exchange: undefined,
    pair: undefined
  }

  componentDidMount() {
    this.props.dispatch(exchangesActions.fetchExchanges())
  }

  onChangeExchange = (e, data) => {
    this.setState({ exchange: data.value })
    this.props.dispatch(exchangesActions.fetchPairs(data.value))
  }

  onChangePair = (e, data) => {
    const pair = this.props.pairs.find(p => p.key === data.value)
    this.setState({ pair: pair })
  }

  onSubmit = coinContainer => {
    this.props.dispatch(coinsActions.add(this.state.pair))
    this.props.history.push("/coin/" + this.state.pair.key)
  }

  render() {
    const exchanges = this.props.exchanges
    const pairs = this.props.pairs
    const ready = !!this.state.pair

    return (
      <FixedModal
        data-orko="addCoinModal"
        closeIcon
        onClose={() => this.props.history.push("/")}
      >
        <Modal.Header>
          <Icon name="bitcoin" />
          Add coin
        </Modal.Header>
        <Modal.Content>
          <Form id="addCoinForm" onSubmit={this.onSubmit}>
            <Form.Field>
              <Dropdown
                basic
                data-orko="selectExchange"
                placeholder="Select exchange"
                fluid
                selection
                loading={exchanges.length === 0}
                value={
                  this.state.exchange ? this.state.exchange.code : undefined
                }
                options={exchanges.map(exchange => ({
                  key: exchange.code,
                  text: exchange.name,
                  value: exchange.code
                }))}
                onChange={this.onChangeExchange}
              />
            </Form.Field>
            <Form.Field>
              <Dropdown
                basic
                data-orko="selectPair"
                placeholder="Select pair"
                fluid
                search
                loading={
                  pairs.length === 0 && this.state.exchange !== undefined
                }
                disabled={pairs.length === 0}
                selection
                options={pairs.map(pair => ({
                  key: pair.key,
                  text: pair.shortName,
                  value: pair.key
                }))}
                onChange={this.onChangePair}
              />
            </Form.Field>
          </Form>
        </Modal.Content>
        <Modal.Actions>
          <Button
            primary
            disabled={!ready}
            data-orko="addCoinSubmit"
            type="submit"
            form="addCoinForm"
          >
            Add
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  }
}

function mapStateToProps(state) {
  return {
    exchanges: state.exchanges.exchanges,
    pairs: state.exchanges.pairs
  }
}

export default connect(mapStateToProps)(AddCoinContainer)
