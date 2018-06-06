import React from "react"
import Immutable from "seamless-immutable"

import Input from "./primitives/Input"
import Checkbox from "./primitives/Checkbox"
import Form from "./primitives/Form"
import Button from "./primitives/Button"

import styled from "styled-components"

const RadioInput = Button.extend`
  margin: 0 ${props => (props.left ? "2px" : "0")} 0
    ${props => (props.right ? "2px" : "0")};
  border-radius: ${props => (props.left ? "3px" : "0")}
    ${props => (props.right ? "3px" : "0")}
    ${props => (props.right ? "3px" : "0")}
    ${props => (props.left ? "3px" : "0")};
  width: 58px;
`

const RadioGroup = styled.div``

const StopTakeProfit = props => {
  const valid =
    props.amountValid &&
    (
      props.highPriceValid ||
      props.lowPriceValid
    )
    &&
    props.lowLimitPriceValid === props.lowPriceValid &&
    props.highLimitPriceValid === props.highPriceValid &&
    (
      (!props.job.lowTrailing && !props.job.highTrailing) ||
      (props.trailingAmountValid)
    )

  const onChange = props.onChange
    ? (prop, value) =>
        props.onChange(
          Immutable.merge(props.job, {
            [prop]: value
          })
        )
    : () => {}

  const onChangeDirection = props.onChange
    ? (value) =>
        props.onChange(
          Immutable.merge(props.job, {
            "direction": value,
            "highTrailing": false,
            "lowTrailing": false
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
          onClick={() => onChangeDirection("BUY")}
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
          onClick={() => onChangeDirection("SELL")}
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
          label={
            (props.job.direction === "BUY" ? "Ask" : "Bid") + " high threshold"
          }
          type="number"
          placeholder="Enter price..."
          value={props.job.highPrice ? props.job.highPrice : ""}
          onChange={e => onChange("highPrice", e.target.value)}
          onFocus={e => props.onFocus("highPrice")}
        />
        <Input
          id="highLimitPrice"
          error={!props.limitPriceValid}
          label="High Limit price"
          type="number"
          placeholder="Enter price..."
          value={props.job.highLimitPrice ? props.job.highLimitPrice : ""}
          onChange={e => onChange("highLimitPrice", e.target.value)}
          onFocus={e => props.onFocus("highLimitPrice")}
        />
        {props.job.direction === "SELL" &&
          <Checkbox
            id="highTrailing"
            label="Trailing"
            type="checkbox"
            checked={props.job.highTrailing}
            onChange={e => onChange("highTrailing", e.target.checked)}
          />
        }
        {props.job.direction === "SELL" && props.job.highTrailing &&
          <Input
            id="trailingAmount"
            error={!props.trailingAmountValid}
            label="Trailing amount"
            type="number"
            placeholder="Enter difference..."
            value={props.job.trailingAmount ? props.job.trailingAmount : ""}
            onChange={e => onChange("trailingAmount", e.target.value)}
            onFocus={e => props.onFocus("trailingAmount")}
          />
        }
        <br />
        <Input
          id="lowPrice"
          error={!props.limitPriceValid}
          label={
            (props.job.direction === "BUY" ? "Ask" : "Bid") + " low threshold"
          }
          type="number"
          placeholder="Enter price..."
          value={props.job.lowPrice ? props.job.lowPrice : ""}
          onChange={e => onChange("lowPrice", e.target.value)}
          onFocus={e => props.onFocus("lowPrice")}
        />
        <Input
          id="lowLimitPrice"
          error={!props.limitPriceValid}
          label="Low limit price"
          type="number"
          placeholder="Enter price..."
          value={props.job.lowLimitPrice ? props.job.lowLimitPrice : ""}
          onChange={e => onChange("lowLimitPrice", e.target.value)}
          onFocus={e => props.onFocus("lowLimitPrice")}
        />
        {props.job.direction === "BUY" &&
          <Checkbox
            id="lowTrailing"
            label="Trailing"
            type="checkbox"
            checked={props.job.lowTrailing}
            onChange={e => onChange("lowTrailing", e.target.checked)}
          />
        }
        {props.job.direction === "BUY" && props.job.lowTrailing &&
          <Input
            id="trailingAmount"
            error={!props.trailingAmountValid}
            label="Trailing amount"
            type="number"
            placeholder="Enter difference..."
            value={props.job.trailingAmount ? props.job.trailingAmount : ""}
            onChange={e => onChange("trailingAmount", e.target.value)}
            onFocus={e => props.onFocus("trailingAmount")}
          />
        }
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
        <Button
          disabled={!valid}
          onClick={props.onSubmit}
          width={120}
          bg={props.job.direction === "BUY" ? "buy" : "sell"}
        >
          {props.job.direction}
        </Button>
      </div>
    </Form>
  )
}

export default StopTakeProfit
