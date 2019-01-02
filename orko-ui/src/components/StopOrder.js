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
      <p>Not supported fully yet. Coming soon.</p>
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
          <input />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        <Form.Input
          id="limitPrice"
          error={!!props.order.limitPrice && !props.limitPriceValid}
          disabled={true}
          label="Limit price"
          labelPosition="right"
          placeholder="Not supported by exchange"
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
        <Form.Button
          title="Submit sell stop order"
          disabled={true || !valid}
          onClick={props.onSell}
          color="red"
        >
          Submit sell stop
        </Form.Button>
        <Form.Button
          title="Submit buy stop order"
          disabled={true || !valid}
          onClick={props.onBuy}
          color="green"
        >
          Submit buy stop
        </Form.Button>
      </FormButtonBar>
    </Form>
  )
}

export default StopOrder
