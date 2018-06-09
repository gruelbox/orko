import React from "react"
import Immutable from "seamless-immutable"

import Input from "./primitives/Input"
import Checkbox from "./primitives/Checkbox"
import Form from "./primitives/Form"
import Button from "./primitives/Button"
import { isValidNumber } from "../util/numberUtils"
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

  const lowLimitPriceValid =
    props.job.lowLimitPrice && isValidNumber(props.job.lowLimitPrice) && props.job.lowLimitPrice > 0
  const highLimitPriceValid =
    props.job.highLimitPrice && isValidNumber(props.job.highLimitPrice) && props.job.highLimitPrice > 0
  const highPriceValid =
    props.job.highPrice && isValidNumber(props.job.highPrice) && props.job.highPrice > 0
  const lowPriceValid =
    props.job.lowPrice && isValidNumber(props.job.lowPrice) && props.job.lowPrice > 0
  const amountValid =
    props.job.amount && isValidNumber(props.job.amount) && props.job.amount > 0
  const initialTrailingStopValid =
    props.job.initialTrailingStop && isValidNumber(props.job.initialTrailingStop) && props.job.initialTrailingStop > 0

  const valid =
    amountValid &&
    (
      highPriceValid ||
      lowPriceValid
    )
    &&
    lowLimitPriceValid === lowPriceValid &&
    highLimitPriceValid === highPriceValid &&
    (
      (!props.job.lowTrailing && !props.job.highTrailing) ||
      (initialTrailingStopValid)
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
          error={!highPriceValid}
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
          error={!highLimitPriceValid}
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
            id="initialTrailingStop"
            error={!initialTrailingStopValid}
            label="Trailing stop price"
            type="number"
            placeholder="Enter price..."
            value={props.job.initialTrailingStop ? props.job.initialTrailingStop : ""}
            onChange={e => onChange("initialTrailingStop", e.target.value)}
            onFocus={e => props.onFocus("initialTrailingStop")}
          />
        }
        <br />
        <Input
          id="lowPrice"
          error={!lowPriceValid}
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
          error={!lowLimitPriceValid}
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
            id="initialTrailingStop"
            error={!initialTrailingStopValid}
            label="Trailing stop price"
            type="number"
            placeholder="Enter difference..."
            value={props.job.initialTrailingStop ? props.job.initialTrailingStop : ""}
            onChange={e => onChange("initialTrailingStop", e.target.value)}
            onFocus={e => props.onFocus("initialTrailingStop")}
          />
        }
        <Input
          id="amount"
          error={!amountValid}
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