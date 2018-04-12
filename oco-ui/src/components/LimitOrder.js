import React from 'react';
import Immutable from 'seamless-immutable';

import Input from './primitives/Input.js';
import Form from './primitives/Form';
import Button from './primitives/Button';

import styled from 'styled-components';
import { fontSize, color, fontWeight, space } from 'styled-system';

const RadioLabel = styled.label.attrs({
  fontSize: 1,
  mt: 0,
  mb: 1,
  p: 0
})`
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const RadioInput = styled.input.attrs({
  type: "radio"
})`
  ${space}
`;

const RadioGroup = styled.div`
`;

const RadioItem = styled.span.attrs({
  type: "radio"
})`
  ${space}
`;

const LimitOrder = props => {
  
  const valid = props.limitPriceValid && props.amountValid;

  const onChange = props.onChange ? (prop, value) => props.onChange(
    Immutable.merge(
      props.job,
      {
        [prop]: value
      }
    )
  ) : () => {};

  return (
    <Form>
      <RadioGroup>
        <RadioItem>
          <RadioInput
            id="BUY" name="direction" value="BUY"
            checked={props.job.direction === 'BUY'}
            onClick={() => onChange("direction", 'BUY')}
          />
          <RadioLabel color="buy" ml={2} for="BUY">Buy</RadioLabel>
        </RadioItem>
        <RadioItem ml={3}>
          <RadioInput
            id="SELL" name="direction" value="BUY"
            checked={props.job.direction === 'SELL'}
            onClick={() => onChange("direction", 'SELL')}
          />
          <RadioLabel color="sell" ml={2} for="SELL">Sell</RadioLabel>
        </RadioItem>
      </RadioGroup>
      <Input
        id="limitPrice"
        error={!props.limitPriceValid}
        label="Limit price"
        type='number'
        placeholder='Enter price...'
        value={props.job.limitPrice ? props.job.limitPrice : ''}
        onChange={e => onChange("limitPrice", e.target.value)}
        onFocus={e => props.onFocus("limitPrice")}
      />
      <Input
        id="amount"
        error={!props.amountValid}
        label="Amount"
        type='number'
        placeholder='Enter amount...'
        value={props.job.amount ? props.job.amount : ''}
        onChange={e => onChange("amount", e.target.value)}
        onFocus={e => props.onFocus("amount")}
      />
      <Button
        disabled={!valid}
        onClick={props.onSubmit}
        bg={props.job.direction === 'BUY' ? 'buy' : 'sell'}
      >
        Submit
      </Button>
    </Form> 
  );
}

export default LimitOrder;