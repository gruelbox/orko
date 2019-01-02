import React from "react"
import Immutable from "seamless-immutable"

import RawForm from "./primitives/RawForm"
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
        <Form.Button disabled={!valid} onClick={props.onSell} color="red">
          Sell
        </Form.Button>
        <Form.Button disabled={!valid} onClick={props.onBuy} color="green">
          Buy
        </Form.Button>
      </FormButtonBar>
    </Form>
  )
}

export default LimitOrder
