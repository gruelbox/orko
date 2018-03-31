import React from 'react';
import { Input, Button, Form, Radio } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import Immutable from 'seamless-immutable';
import { BUY, SELL } from '../store/limitOrder/reducer';

const LimitOrder = props => {

  const isValidNumber = (val) => !isNaN(val) && val > 0 && val !== '';

  const color = props.job.direction === "BUY" ? 'green' : 'red';
  const priceValid = isValidNumber(props.job.price);
  const amountValid = isValidNumber(props.job.amount);

  const onChange = props.onChange
    ? (prop, value) => props.onChange(
      Immutable.merge(
        props.job,
        {
          [prop]: value
        }
      )
    )
    : () => {};

  return (
    <div>
      <Form.Group grouped>
        <label>Direction</label>
        <Form.Field
          control={Radio}
          label='Buy'
          value={BUY}
          checked={props.job.direction === BUY}
          onChange={e => onChange("direction", BUY)}
        />
        <Form.Field
          control={Radio}
          label='Sell'
          value={SELL}
          checked={props.job.direction === SELL}
          onChange={e => onChange("direction", SELL)}
        />
      </Form.Group>
      <Form.Group widths='equal'>
        <Form.Field required>
          <label>Limit price</label>
          <Input fluid type='text' placeholder="Enter limit price..." action error={!priceValid}>
            <input value={props.job.price} onChange={e => onChange("price", e.target.value)} />
            <Button color={color} size="mini" onClick={props.setBidPrice}>B</Button>
            <Button color={color} size="mini" onClick={props.setAskPrice}>A</Button>
            <Button color={color} size="mini" onClick={props.onSetMarketPrice}>M</Button>
          </Input>
        </Form.Field> 
        <Form.Input
          fluid
          required
          label="Amount"
          type='text'
          placeholder='Enter amount...'
          value={props.job.amount || ''}
          onChange={e => onChange("amount", e.target.value)}
          error={!amountValid}
        />
      </Form.Group>
    </div>
  );
};

export default LimitOrder;

export const limitOrderShape = {
  direction: PropTypes.string.isRequired,
  price: PropTypes.string.isRequired,
  amount: PropTypes.string.isRequired
};

LimitOrder.propTypes = {
  job: PropTypes.shape(limitOrderShape).isRequired,
  onChange: PropTypes.func,
  onSetBidPrice: PropTypes.func,
  onSetAskPrice: PropTypes.func,
  onSetMarketPrice: PropTypes.func,
};