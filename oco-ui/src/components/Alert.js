import React from 'react';
import { Form } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import Immutable from 'seamless-immutable';
import { shape } from '../store/alert/reducer';

const Alert = props => {

  const isValidNumber = (val) => !isNaN(val) && val !== '' && val > 0;
  const highPriceValid = props.job.highPrice && isValidNumber(props.job.highPrice);
  const lowPriceValid = props.job.lowPrice && isValidNumber(props.job.lowPrice);
  const messageValid = props.job.message !== "";

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
      <Form.Input
        error={!highPriceValid}
        label="Price rises above"
        type='text'
        placeholder='Enter price...'
        value={props.job.highPrice ? props.job.highPrice : ''}
        onChange={e => onChange("highPrice", e)}
        onFocus={e => props.onFocus("highPrice")}
      />
      <Form.Input
        error={!lowPriceValid}
        label="Price drops below"
        type='text'
        placeholder='Enter price...'
        value={props.job.lowPrice ? props.job.lowPrice : ''}
        onChange={e => onChange("lowPrice", e)}
        onFocus={e => props.onFocus("lowPrice")}
      />
      <Form.Input
        error={!messageValid}
        label="Message"
        type='text'
        placeholder='Enter message...'
        value={props.job.message}
        onChange={e => onChange("message", e)}
      />
    </div> 
  );
}

export default Alert;

Alert.propTypes = {
  job: PropTypes.shape(shape).isRequired,
  onChange: PropTypes.func
};