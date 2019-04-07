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
import InlineButton from "./primitives/InlineButton"
import FormButtonBar from "./primitives/FormButtonBar"
import { Form, Label } from "semantic-ui-react"

const LimitOrder = props => {
  const valid = props.limitPriceValid && props.amountValid

  const onChange = props.onChange
    ? (prop, value) =>
        props.onChange(
          Immutable.merge(props.order, {
            [prop]: value
          })
        )
    : () => {}

  return (
    <Form data-orko="limitOrder" as={RawForm}>
      <Form.Group>
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
          <input data-orko="limitPrice" />
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
          <input data-orko="amount" />
          <Label>{props.coin.base}</Label>
          {props.limitPriceValid && (
            <InlineButton.Container>
              <InlineButton.Button
                onClick={() => props.onSetMaxAmount("SELL")}
                color="sell"
                title="Set the amount to the maximum possible to sell at the specified limit price."
              >
                S
              </InlineButton.Button>
              <InlineButton.Button
                onClick={() => props.onSetMaxAmount("BUY")}
                color="buy"
                title="Set the amount to the maximum possible to buy at the specified limit price."
              >
                B
              </InlineButton.Button>
            </InlineButton.Container>
          )}
        </Form.Input>
      </Form.Group>
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
        <Form.Button
          disabled={!valid}
          onClick={props.onSell}
          color="red"
          data-orko="sell"
        >
          Sell
        </Form.Button>
        <Form.Button
          disabled={!valid}
          onClick={props.onBuy}
          color="green"
          data-orko="buy"
        >
          Buy
        </Form.Button>
      </FormButtonBar>
    </Form>
  )
}

export default LimitOrder
