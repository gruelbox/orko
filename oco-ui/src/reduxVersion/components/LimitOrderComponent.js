import React from 'react';
import { Input, Button, Form } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import Immutable from 'seamless-immutable';

const LimitOrderComponent = props => {

  const isValidNumber = (val) => !isNaN(val) && val > 0 && val !== '';

  const color = props.job.direction === "BUY" ? 'green' : 'red';
  const priceValid = isValidNumber(props.job.price);
  const amountValid = isValidNumber(props.job.amount);

  const onChange = props.onChange ? (prop, e) => props.onChange(
    Immutable.merge(
      props.job,
      {
        [prop]: e.target.value
      }
    )
  ) : () => {};

  return (
    <div>
      <Form.Group widths='equal'>
        <Form.Field required>
          <label>Limit price</label>
          <Input fluid type='text' placeholder="Enter limit price..." action error={!priceValid}>
            <input value={props.job.price} onChange={e => onChange("price", e)} />
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
          onChange={e => onChange("amount", e)}
          error={!amountValid}
        />
      </Form.Group>
    </div>
  );
};

export default LimitOrderComponent;

export const limitOrderShape = {
  direction: PropTypes.string.isRequired,
  price: PropTypes.string.isRequired,
  amount: PropTypes.string.isRequired
};

LimitOrderComponent.propTypes = {
  job: PropTypes.shape(limitOrderShape).isRequired,
  onChange: PropTypes.func,
  onSetBidPrice: PropTypes.func,
  onSetAskPrice: PropTypes.func,
  onSetMarketPrice: PropTypes.func,
};