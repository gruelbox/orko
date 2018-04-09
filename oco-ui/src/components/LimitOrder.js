import React from 'react';
import Immutable from 'seamless-immutable';

import Input from './primitives/Input.js';
import Form from './primitives/Form';
import Button from './primitives/Button';

const LimitOrder = props => {
  
  const valid = props.limitPriceValid && props.amountValid;

  const onChange = props.onChange ? (prop, e) => props.onChange(
    Immutable.merge(
      props.job,
      {
        [prop]: e.target.value
      }
    )
  ) : () => {};

  return (
    <Form>
      <Input
        id="amount"
        error={!props.amountValid}
        label="Amount"
        type='number'
        placeholder='Enter amount...'
        value={props.job.amount ? props.job.amount : ''}
        onChange={e => onChange("amount", e)}
        onFocus={e => props.onFocus("amount")}
      />
      <Input
        id="limitPrice"
        error={!props.limitPriceValid}
        label="Limit price"
        type='number'
        placeholder='Enter price...'
        value={props.job.limitPrice ? props.job.limitPrice : ''}
        onChange={e => onChange("limitPrice", e)}
        onFocus={e => props.onFocus("limitPrice")}
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