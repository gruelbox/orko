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

import * as coinsActions from "../store/coins/actions"
import { connect } from "react-redux"
import { Icon, Button, Form, Dropdown, Modal } from "semantic-ui-react"
import FixedModal from "../components/primitives/FixedModal"
import { withAuth } from "@orko-ui-auth/index"
import { withMarket, augmentCoin } from "@orko-ui-market/index"
import { withLog } from "@orko-ui-log/index"
import exchangesService from "@orko-ui-market/exchangesService"

class AddCoinContainer extends Component {
  state = {
    pairs: [],
    exchange: undefined,
    pair: undefined
  }

  componentDidMount() {
    this.props.marketApi.actions.refreshExchanges()
  }

  onChangeExchange = (e, data) => {
    const exchange = data.value
    this.setState({ exchange })
    this.props.logApi.trace("Fetching pairs for exchange: " + exchange)
    this.props.auth
      .authenticatedRequest(() => exchangesService.fetchPairs(exchange))
      .then(pairs => {
        this.setState({ pairs: pairs.map(p => augmentCoin(p, exchange)) })
        this.props.logApi.trace(pairs.length + " pairs fetched")
      })
      .catch(error => this.props.logApi.errorPopup(error.message))
  }

  onChangePair = (e, data) => {
    const pair = this.state.pairs.find(p => p.key === data.value)
    this.setState({ pair })
  }

  onSubmit = () => {
    this.props.dispatch(coinsActions.add(this.props.auth, this.state.pair))
    this.props.history.push("/coin/" + this.state.pair.key)
  }

  render() {
    const exchanges = this.props.marketApi.data.exchanges
    const ready = !!this.state.pair

    return (
      <FixedModal data-orko="addCoinModal" closeIcon onClose={() => this.props.history.push("/")}>
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
                value={this.state.exchange ? this.state.exchange.code : undefined}
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
                loading={this.state.pairs.length === 0 && this.state.exchange !== undefined}
                disabled={this.state.pairs.length === 0}
                selection
                options={this.state.pairs.map(pair => ({
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
          <Button primary disabled={!ready} data-orko="addCoinSubmit" type="submit" form="addCoinForm">
            Add
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  }
}

function mapStateToProps(state) {
  return {}
}

export default withLog(withMarket(withAuth(connect(mapStateToProps)(AddCoinContainer))))
