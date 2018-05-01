import React from "react"
import Immutable from "seamless-immutable"

import Input from "./primitives/Input"
import Form from "./primitives/Form"
import Button from "./primitives/Button"

import styled from "styled-components"

const RadioInput = Button.extend`
  margin:
    0
    ${props => props.left ? "2px" : "0"}
    0
    ${props => props.right ? "2px" : "0"};
  border-radius:
    ${props => props.left ? "3px" : "0"}
    ${props => props.right ? "3px" : "0"}
    ${props => props.right ? "3px" : "0"}
    ${props => props.left ? "3px" : "0"};
  width: 58px;
`

const RadioGroup = styled.div``

const StopTakeProfit = props => {
  const valid = props.limitPriceValid && props.amountValid &&
    (props.highPriceValid || props.lowPriceValid)

  const onChange = props.onChange
    ? (prop, value) =>
        props.onChange(
          Immutable.merge(props.job, {
            [prop]: value
          })
        )
    : () => {}

  return (
    <Form>
      <RadioGroup>
        <RadioInput
          id="BUY"
          name="direction"
          value="BUY"
          checked={props.job.direction === "BUY"}
          onClick={() => onChange("direction", "BUY")}
          left
          bg={props.job.direction === "BUY" ? "buy" : "deemphasis"}
        >
          Buy
        </RadioInput>
        <RadioInput
          id="SELL"
          name="direction"
          value="BUY"
          checked={props.job.direction === "SELL"}
          onClick={() => onChange("direction", "SELL")}
          right
          bg={props.job.direction === "SELL" ? "sell" : "deemphasis"}
        >
          Sell
        </RadioInput>
      </RadioGroup>
      <div>
        <Input
          id="highPrice"
          error={!props.limitPriceValid}
          label="High threshold"
          type="number"
          placeholder="Enter price..."
          value={props.job.highPrice ? props.job.highPrice : ""}
          onChange={e => onChange("highPrice", e.target.value)}
          onFocus={e => props.onFocus("highPrice")}
        />
        <Input
          id="lowPrice"
          error={!props.limitPriceValid}
          label="Low threshold"
          type="number"
          placeholder="Enter price..."
          value={props.job.lowPrice ? props.job.lowPrice : ""}
          onChange={e => onChange("lowPrice", e.target.value)}
          onFocus={e => props.onFocus("lowPrice")}
        />
        <Input
          id="limitPrice"
          error={!props.limitPriceValid}
          label="Limit price"
          type="number"
          placeholder="Enter price..."
          value={props.job.limitPrice ? props.job.limitPrice : ""}
          onChange={e => onChange("limitPrice", e.target.value)}
          onFocus={e => props.onFocus("limitPrice")}
        />
        <Input
          id="amount"
          error={!props.amountValid}
          label="Amount"
          type="number"
          placeholder="Enter amount..."
          value={props.job.amount ? props.job.amount : ""}
          onChange={e => onChange("amount", e.target.value)}
          onFocus={e => props.onFocus("amount")}
        />
      </div>
      <Button
        disabled={!valid}
        onClick={props.onSubmit}
        width={120}
        bg={props.job.direction === "BUY" ? "buy" : "sell"}
      >
        {props.job.direction}
      </Button>
    </Form>
  )
}

export default StopTakeProfit
