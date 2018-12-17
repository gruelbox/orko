import React from "react"
import Immutable from "seamless-immutable"

import Input from "./primitives/Input"
import Form from "./primitives/Form"
import Button from "./primitives/Button"

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
    <Form
      data-orko="limitOrder"
      buttons={() => (
        <>
          <Button
            disabled={!valid}
            onClick={props.onSell}
            width={120}
            bg="sell"
            mr={1}
          >
            SELL
          </Button>
          <Button
            disabled={!valid}
            onClick={props.onBuy}
            width={120}
            bg="buy"
            ml={1}
          >
            BUY
          </Button>
        </>
      )}
    >
      <Input
        id="limitPrice"
        error={props.order.limitPrice && !props.limitPriceValid}
        label="Limit price"
        type="number"
        placeholder="Enter price..."
        value={props.order.limitPrice ? props.order.limitPrice : ""}
        onChange={e => onChange("limitPrice", e.target.value)}
        onFocus={e => props.onFocus("limitPrice")}
        width="140px"
      />
      <Input
        id="amount"
        error={props.order.amount && !props.amountValid}
        label="Amount"
        type="number"
        placeholder="Enter amount..."
        value={props.order.amount ? props.order.amount : ""}
        onChange={e => onChange("amount", e.target.value)}
        onFocus={e => props.onFocus("amount")}
        width="140px"
      />
    </Form>
  )
}

export default LimitOrder
