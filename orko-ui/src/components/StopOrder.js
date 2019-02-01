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

const StopOrder = props => {
  const valid =
    props.stopPriceValid && props.limitPriceValid && props.amountValid

  const onChange = props.onChange
    ? (prop, value) =>
        props.onChange(
          Immutable.merge(props.order, {
            [prop]: value
          })
        )
    : () => {}

  return (
    <Form data-orko="stopOrder" as={RawForm}>
      <Form.Group>
        <Form.Input
          id="stopPrice"
          required
          error={!!props.order.stopPrice && !props.stopPriceValid}
          label="Stop price"
          labelPosition="right"
          placeholder="Enter stop price..."
          value={props.order.stopPrice ? props.order.stopPrice : ""}
          onChange={e => onChange("stopPrice", e.target.value)}
          onFocus={e => props.onFocus("stopPrice")}
        >
          <input data-orko="stopPrice" />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        <Form.Input
          id="limitPrice"
          error={!!props.order.limitPrice && !props.limitPriceValid}
          disabled={!props.allowLimit}
          required={!props.allowMarket}
          label="Limit price"
          labelPosition="right"
          placeholder={
            props.allowLimit
              ? "Enter limit price..."
              : "Not supported for exchange"
          }
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
        </Form.Input>
      </Form.Group>
      <Form.Checkbox
        title="If enabled and supported, runs the stop on the exchange itself. This will usually incur less slippage, but locks the balance, and is not supported on all exchanges."
        id="onExchange"
        data-orko="onExchange"
        label="Place on exchange"
        checked={props.order.useExchange}
        onChange={e => onChange("useExchange", e.target.checked)}
        disabled={!props.allowServerSide}
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
        <Form.Button
          title="Submit sell stop order"
          disabled={!valid || !props.onSell}
          onClick={props.onSell}
          data-orko="sell"
          color="red"
        >
          {!!props.onSell
            ? props.order.useExchange
              ? "Sell stop"
              : "Soft sell stop"
            : "Sell stop not supported"}
        </Form.Button>
        <Form.Button
          title="Submit buy stop order"
          disabled={!valid || !props.onBuy}
          onClick={props.onBuy}
          data-orko="buy"
          color="green"
        >
          {!!props.onBuy
            ? props.order.useExchange
              ? "Buy stop"
              : "Soft buy stop"
            : "Buy stop not supported"}
        </Form.Button>
      </FormButtonBar>
    </Form>
  )
}

export default StopOrder
