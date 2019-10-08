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
import { isValidNumber } from "modules/common/util/numberUtils"
import RawForm from "./primitives/RawForm"
import FormButtonBar from "./primitives/FormButtonBar"
import { Form, Label, Button } from "semantic-ui-react"

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
    <Form data-orko="stopTakeProfit" as={RawForm}>
      <Form.Group>
        <Form.Input
          id="highPrice"
          error={!!props.job.highPrice && !highPriceValid}
          label={
            "If " +
            (props.job.direction === "BUY" ? "ask" : "bid") +
            " price rises above..."
          }
          labelPosition="right"
          placeholder="Enter trigger price..."
          value={props.job.highPrice ? props.job.highPrice : ""}
          onChange={e => onChange("highPrice", e.target.value)}
          onFocus={e => props.onFocus("highPrice")}
        >
          <input data-orko="highPrice" />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        <Form.Input
          id="highLimitPrice"
          error={!!props.job.highLimitPrice && !highLimitPriceValid}
          label={
            "... then " +
            (props.job.direction === "BUY" ? "buy" : "sell") +
            " at"
          }
          labelPosition="right"
          placeholder={
            "Enter high " +
            (props.job.direction === "BUY" ? "buy" : "sell") +
            " price..."
          }
          value={props.job.highLimitPrice ? props.job.highLimitPrice : ""}
          onChange={e => onChange("highLimitPrice", e.target.value)}
          onFocus={e => props.onFocus("highLimitPrice")}
        >
          <input data-orko="highLimitPrice" />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        {props.job.direction === "SELL" && props.job.highTrailing && (
          <Form.Input
            id="initialTrailingStop"
            error={!!props.job.initialTrailingStop && !initialTrailingStopValid}
            label="... with trailing stop starting at"
            labelPosition="right"
            placeholder="Price (above trigger price)..."
            value={
              props.job.initialTrailingStop ? props.job.initialTrailingStop : ""
            }
            onChange={e => onChange("initialTrailingStop", e.target.value)}
            onFocus={e => props.onFocus("initialTrailingStop")}
          >
            <input data-orko="initialTrailingStop" />
            <Label>{props.coin.counter}</Label>
          </Form.Input>
        )}
      </Form.Group>
      <Form.Group>
        <Form.Input
          id="lowPrice"
          error={!!props.job.lowPrice && !lowPriceValid}
          label={
            "If " +
            (props.job.direction === "BUY" ? "ask" : "bid") +
            " price drops below..."
          }
          labelPosition="right"
          placeholder="Enter trigger price..."
          value={props.job.lowPrice ? props.job.lowPrice : ""}
          onChange={e => onChange("lowPrice", e.target.value)}
          onFocus={e => props.onFocus("lowPrice")}
        >
          <input data-orko="lowPrice" />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        <Form.Input
          id="lowLimitPrice"
          error={!!props.job.lowLimitPrice && !lowLimitPriceValid}
          label={
            "... then " +
            (props.job.direction === "BUY" ? "buy" : "sell") +
            " at"
          }
          labelPosition="right"
          placeholder={
            "Enter low " +
            (props.job.direction === "BUY" ? "buy" : "sell") +
            " price..."
          }
          value={props.job.lowLimitPrice ? props.job.lowLimitPrice : ""}
          onChange={e => onChange("lowLimitPrice", e.target.value)}
          onFocus={e => props.onFocus("lowLimitPrice")}
        >
          <input data-orko="lowLimitPrice" />
          <Label>{props.coin.counter}</Label>
        </Form.Input>
        {props.job.direction === "BUY" && props.job.lowTrailing && (
          <Form.Input
            id="initialTrailingStop"
            error={!!props.job.initialTrailingStop && !initialTrailingStopValid}
            label="... with trailing stop starting at"
            labelPosition="right"
            placeholder="Price (below trigger price)..."
            value={
              props.job.initialTrailingStop ? props.job.initialTrailingStop : ""
            }
            onChange={e => onChange("initialTrailingStop", e.target.value)}
            onFocus={e => props.onFocus("initialTrailingStop")}
          >
            <input data-orko="initialTrailingStop" />
            <Label>{props.coin.counter}</Label>
          </Form.Input>
        )}
      </Form.Group>
      <Form.Group style={{ flex: "1" }}>
        <Form.Input
          id="amount"
          required
          error={!!props.job.amount && !amountValid}
          label="Amount"
          labelPosition="right"
          placeholder="Enter amount..."
          value={props.job.amount ? props.job.amount : ""}
          onChange={e => onChange("amount", e.target.value)}
          onFocus={e => props.onFocus("amount")}
        >
          <input data-orko="amount" />
          <Label>{props.coin.base}</Label>
        </Form.Input>
      </Form.Group>
      <FormButtonBar>
        <Form.Group>
          {props.job.direction === "BUY" && (
            <Form.Checkbox
              title="Enable trailing buy stop"
              id="lowTrailing"
              label="Trailing"
              checked={props.job.lowTrailing}
              style={{ verticalAlign: "middle" }}
              onChange={e => onChange("lowTrailing", e.target.checked)}
            />
          )}
          {props.job.direction === "SELL" && (
            <Form.Checkbox
              title="Enable trailing sell stop"
              id="highTrailing"
              label="Trailing"
              checked={props.job.highTrailing}
              inline
              onChange={e => onChange("highTrailing", e.target.checked)}
            />
          )}
          <Form.Checkbox
            title="Use margin account (if supported by the exchange)"
            id="useMargin"
            label="Use margin"
            checked={false}
            onChange={e => onChange("useMargin", e.target.checked)}
            disabled={true}
          />
        </Form.Group>
        <Button.Group>
          <Button
            id="BUY"
            data-orko="BUY"
            onClick={() => onChangeDirection("BUY")}
            color={props.job.direction === "BUY" ? "green" : "black"}
          >
            Buy
          </Button>
          <Button.Or />
          <Button
            id="SELL"
            data-orko="SELL"
            onClick={() => onChangeDirection("SELL")}
            color={props.job.direction === "SELL" ? "red" : "black"}
          >
            Sell
          </Button>
        </Button.Group>
        <Form.Button
          data-orko="submitOrder"
          disabled={!valid}
          onClick={props.onSubmit}
          color={props.job.direction === "BUY" ? "green" : "red"}
        >
          {props.job.direction}
        </Form.Button>
      </FormButtonBar>
    </Form>
  )
}

export default StopTakeProfit
