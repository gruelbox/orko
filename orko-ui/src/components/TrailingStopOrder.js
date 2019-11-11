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
import React from "react"
import Immutable from "seamless-immutable"

import RawForm from "./primitives/RawForm"
import FormButtonBar from "./primitives/FormButtonBar"
import { Form, Label } from "semantic-ui-react"

const TrailingStopOrder = props => {
  const valid = props.stopPriceValid && props.limitPriceValid && props.amountValid && props.tickerAvailable

  const onChange = props.onChange
    ? (prop, value) =>
        props.onChange(
          Immutable.merge(props.order, {
            [prop]: value
          })
        )
    : () => {}

  return (
    <Form data-orko="trailingStopOrder" as={RawForm}>
      <Form.Group>
        <Form.Input
          id="stopPrice"
          required
          error={!!props.order.stopPrice && !props.stopPriceValid}
          label="Initial stop price"
          labelPosition="right"
          placeholder="Enter initial stop price..."
          value={props.order.stopPrice ? props.order.stopPrice : ""}
          onChange={e => onChange("stopPrice", e.target.value)}
          onFocus={e => props.onFocus("stopPrice")}
        >
          <input />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        <Form.Input
          id="limitPrice"
          required
          error={!!props.order.limitPrice && !props.limitPriceValid}
          label="Limit price"
          labelPosition="right"
          placeholder="Enter price..."
          value={props.order.limitPrice ? props.order.limitPrice : ""}
          onChange={e => onChange("limitPrice", e.target.value)}
          onFocus={e => props.onFocus("limitPrice")}
        >
          <input />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        <Form.Input
          id="amount"
          required
          error={!!props.order.amount && !props.amountValid}
          label="Amount"
          labelPosition="right"
          placeholder="Enter amount..."
          value={props.order.amount ? props.order.amount : ""}
          onChange={e => onChange("amount", e.target.value)}
          onFocus={e => props.onFocus("amount")}
        >
          <input />
          <Label>{props.coin.base}</Label>
        </Form.Input>
      </Form.Group>
      <Form.Checkbox
        title="If enabled and supported, runs the stop on the exchange itself. This will usually incur less slippage, but locks the balance, and is not supported on all exchanges."
        id="onExchange"
        label="Place on exchange"
        checked={props.order.useExchange}
        onChange={e => onChange("useExchange", e.target.checked)}
        disabled={true}
      />
      <Form.Group style={{ flex: "1" }}>
        <Form.Checkbox
          title="Use margin account (if supported by the exchange)"
          id="useMargin"
          label="Use margin"
          checked={false}
          onChange={e => onChange("useMargin", e.target.checked)}
          disabled={true}
        />
      </Form.Group>
      <FormButtonBar>
        <Form.Button title="Submit sell stop order" disabled={!valid} onClick={props.onSell} color="red">
          Submit sell stop
        </Form.Button>
        <Form.Button title="Submit buy stop order" disabled={!valid} onClick={props.onBuy} color="green">
          Submit buy stop
        </Form.Button>
      </FormButtonBar>
    </Form>
  )
}

export default TrailingStopOrder
