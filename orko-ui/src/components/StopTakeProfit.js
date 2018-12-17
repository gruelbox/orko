import React from "react"
import Immutable from "seamless-immutable"

import Input from "./primitives/Input"
import Checkbox from "./primitives/Checkbox"
import Form from "./primitives/Form"
import Button from "./primitives/Button"
import { isValidNumber } from "../util/numberUtils"
import styled from "styled-components"

const RadioInput = styled(Button)`
  margin-left: ${props => (props.left ? "inherit" : "0 !important")};
  margin-right: ${props => (props.right ? "inherit" : "0 !important")};
  border-radius: ${props => (props.left ? "3px" : "0")}
    ${props => (props.right ? "3px" : "0")}
    ${props => (props.right ? "3px" : "0")}
    ${props => (props.left ? "3px" : "0")};
  width: 58px;
`

const StopTakeProfit = props => {
  const lowLimitPriceValid =
    props.job.lowLimitPrice &&
    isValidNumber(props.job.lowLimitPrice) &&
    props.job.lowLimitPrice > 0
  const highLimitPriceValid =
    props.job.highLimitPrice &&
    isValidNumber(props.job.highLimitPrice) &&
    props.job.highLimitPrice > 0
  const highPriceValid =
    props.job.highPrice &&
    isValidNumber(props.job.highPrice) &&
    props.job.highPrice > 0
  const lowPriceValid =
    props.job.lowPrice &&
    isValidNumber(props.job.lowPrice) &&
    props.job.lowPrice > 0
  const amountValid =
    props.job.amount && isValidNumber(props.job.amount) && props.job.amount > 0
  const initialTrailingStopValid =
    props.job.initialTrailingStop &&
    isValidNumber(props.job.initialTrailingStop) &&
    props.job.initialTrailingStop > 0

  const valid =
    amountValid &&
    (highPriceValid || lowPriceValid) &&
    lowLimitPriceValid === lowPriceValid &&
    highLimitPriceValid === highPriceValid &&
    ((!props.job.lowTrailing && !props.job.highTrailing) ||
      initialTrailingStopValid)

  const onChange = props.onChange
    ? (prop, value) =>
        props.onChange(
          Immutable.merge(props.job, {
            [prop]: value
          })
        )
    : () => {}

  const onChangeDirection = props.onChange
    ? value =>
        props.onChange(
          Immutable.merge(props.job, {
            direction: value,
            highTrailing: false,
            lowTrailing: false
          })
        )
    : () => {}

  return (
    <Form
      data-orko="stopTakeProfit"
      buttons={() => (
        <>
          {props.job.direction === "BUY" && (
            <Checkbox
              title="Enable trailing buy stop"
              id="lowTrailing"
              label="Trailing"
              type="checkbox"
              checked={props.job.lowTrailing}
              onChange={e => onChange("lowTrailing", e.target.checked)}
            />
          )}
          {props.job.direction === "SELL" && (
            <Checkbox
              title="Enable trailing sell stop"
              id="highTrailing"
              label="Trailing"
              type="checkbox"
              checked={props.job.highTrailing}
              onChange={e => onChange("highTrailing", e.target.checked)}
            />
          )}
          <RadioInput
            id="BUY"
            data-orko="BUY"
            checked={props.job.direction === "BUY"}
            onClick={() => onChangeDirection("BUY")}
            left
            bg={props.job.direction === "BUY" ? "buy" : "deemphasis"}
          >
            Buy
          </RadioInput>
          <RadioInput
            id="SELL"
            data-orko="SELL"
            checked={props.job.direction === "SELL"}
            onClick={() => onChangeDirection("SELL")}
            right
            bg={props.job.direction === "SELL" ? "sell" : "deemphasis"}
          >
            Sell
          </RadioInput>
          <Button
            data-orko="submitOrder"
            disabled={!valid}
            onClick={props.onSubmit}
            width={120}
            bg={props.job.direction === "BUY" ? "buy" : "sell"}
          >
            {props.job.direction}
          </Button>
        </>
      )}
    >
      <Input
        id="highPrice"
        error={props.job.highPrice && !highPriceValid}
        label={
          (props.job.direction === "BUY" ? "Ask" : "Bid") + " high threshold"
        }
        type="number"
        placeholder="Enter price..."
        value={props.job.highPrice ? props.job.highPrice : ""}
        onChange={e => onChange("highPrice", e.target.value)}
        onFocus={e => props.onFocus("highPrice")}
        width="140px"
      />
      <Input
        id="highLimitPrice"
        error={props.job.highLimitPrice && !highLimitPriceValid}
        label="High Limit price"
        type="number"
        placeholder="Enter price..."
        value={props.job.highLimitPrice ? props.job.highLimitPrice : ""}
        onChange={e => onChange("highLimitPrice", e.target.value)}
        onFocus={e => props.onFocus("highLimitPrice")}
        width="140px"
      />
      {props.job.direction === "SELL" && props.job.highTrailing && (
        <Input
          id="initialTrailingStop"
          error={props.job.initialTrailingStop && !initialTrailingStopValid}
          label="Trailing stop price"
          type="number"
          placeholder="Enter price..."
          value={
            props.job.initialTrailingStop ? props.job.initialTrailingStop : ""
          }
          onChange={e => onChange("initialTrailingStop", e.target.value)}
          onFocus={e => props.onFocus("initialTrailingStop")}
          width="140px"
        />
      )}
      <Input
        id="lowPrice"
        error={props.job.lowPrice && !lowPriceValid}
        label={
          (props.job.direction === "BUY" ? "Ask" : "Bid") + " low threshold"
        }
        type="number"
        placeholder="Enter price..."
        value={props.job.lowPrice ? props.job.lowPrice : ""}
        onChange={e => onChange("lowPrice", e.target.value)}
        onFocus={e => props.onFocus("lowPrice")}
        width="140px"
      />
      <Input
        id="lowLimitPrice"
        error={props.job.lowLimitPrice && !lowLimitPriceValid}
        label="Low limit price"
        type="number"
        placeholder="Enter price..."
        value={props.job.lowLimitPrice ? props.job.lowLimitPrice : ""}
        onChange={e => onChange("lowLimitPrice", e.target.value)}
        onFocus={e => props.onFocus("lowLimitPrice")}
        width="140px"
      />
      {props.job.direction === "BUY" && props.job.lowTrailing && (
        <Input
          id="initialTrailingStop"
          error={props.job.initialTrailingStop && !initialTrailingStopValid}
          label="Trailing stop price"
          type="number"
          placeholder="Enter difference..."
          value={
            props.job.initialTrailingStop ? props.job.initialTrailingStop : ""
          }
          onChange={e => onChange("initialTrailingStop", e.target.value)}
          onFocus={e => props.onFocus("initialTrailingStop")}
          width="140px"
        />
      )}
      <Input
        id="amount"
        error={props.job.amount && !amountValid}
        label="Amount"
        type="number"
        placeholder="Enter amount..."
        value={props.job.amount ? props.job.amount : ""}
        onChange={e => onChange("amount", e.target.value)}
        onFocus={e => props.onFocus("amount")}
        width="140px"
      />
    </Form>
  )
}

export default StopTakeProfit
