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
import React, { useState, useEffect, useContext } from "react"

import { connect } from "react-redux"
import { Icon, Button, Form, Dropdown, Modal } from "semantic-ui-react"
import FixedModal from "../components/primitives/FixedModal"
import { AuthContext } from "modules/auth"
import { augmentCoin, Coin, MarketContext, PartialServerCoin, Exchange } from "modules/market"
import { LogContext } from "modules/log"
import exchangesService from "modules/market/exchangesService"
import { ServerContext } from "modules/server"

const AddCoinContainer: React.FC<{ dispatch; history }> = ({ dispatch, history }) => {
  const marketApi = useContext(MarketContext)
  const logApi = useContext(LogContext)
  const authApi = useContext(AuthContext)
  const serverApi = useContext(ServerContext)

  const [pairs, setPairs] = useState<Array<Coin>>([])
  const [exchange, setExchange] = useState<Exchange>(null)
  const [pair, setPair] = useState<Coin>(null)

  const refreshExchanges = marketApi.actions.refreshExchanges
  useEffect(() => {
    refreshExchanges()
  }, [refreshExchanges])

  const onChangeExchange = (e, data) => {
    const exchange = data.value
    setExchange(marketApi.data.exchanges.find(e => e.code === data.value))
    logApi.trace("Fetching pairs for exchange: " + exchange)
    authApi
      .authenticatedRequest(() => exchangesService.fetchPairs(exchange))
      .then((pairs: Array<PartialServerCoin>) => {
        setPairs(pairs.map(p => augmentCoin(p, exchange)))
        logApi.trace(pairs.length + " pairs fetched")
      })
      .catch(error => logApi.errorPopup(error.message))
  }

  const onChangePair = (e, data) => {
    setPair(pairs.find(p => p.key === data.value))
  }

  const onSubmit = () => {
    serverApi.addSubscription(pair)
    history.push("/coin/" + pair.key)
  }

  const ready = !!pair

  return (
    <FixedModal data-orko="addCoinModal" closeIcon onClose={() => history.push("/")}>
      <Modal.Header>
        <Icon name="bitcoin" />
        Add coin
      </Modal.Header>
      <Modal.Content>
        <Form id="addCoinForm" onSubmit={onSubmit}>
          <Form.Field>
            <Dropdown
              basic
              data-orko="selectExchange"
              placeholder="Select exchange"
              fluid
              selection
              loading={marketApi.data.exchanges.length === 0}
              value={exchange ? exchange.code : undefined}
              options={marketApi.data.exchanges.map(exchange => ({
                key: exchange.code,
                text: exchange.name,
                value: exchange.code
              }))}
              onChange={onChangeExchange}
            />
          </Form.Field>
          <Form.Field>
            <Dropdown
              basic
              data-orko="selectPair"
              placeholder="Select pair"
              fluid
              search
              loading={pairs.length === 0 && exchange !== undefined}
              disabled={pairs.length === 0}
              selection
              options={pairs.map(pair => ({
                key: pair.key,
                text: pair.shortName,
                value: pair.key
              }))}
              onChange={onChangePair}
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

export default connect(() => {})(AddCoinContainer)
