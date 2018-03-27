import React from 'react';
import { Form } from 'semantic-ui-react'
import PropTypes from 'prop-types';

const Alert = props => {

  const isValidNumber = (val) => !isNaN(val) && val !== '' && val > 0;
  const highPriceValid = props.job.high && isValidNumber(props.job.high.thresholdAsString);
  const lowPriceValid = props.job.low && isValidNumber(props.job.low.thresholdAsString);
  const messageValid = props.job.message !== "";

  return (
    <div>
      <Form.Input
        error={!highPriceValid}
        label="Price rises above"
        type='text'
        placeholder='Enter price...'
        value={props.job.high ? props.job.high.thresholdAsString : ''}
        onChange={props.onChangeHighPrice}
      />
      <Form.Input
        error={!lowPriceValid}
        label="Price drops below"
        type='text'
        placeholder='Enter price...'
        value={props.job.low ? props.job.low.thresholdAsString : ''}
        onChange={props.onChangeLowPrice}
      />
      <Form.Input
        error={!messageValid}
        label="Message"
        type='text'
        placeholder='Enter message...'
        value={props.job.message}
        onChange={props.onChangeMessage}
      />
    </div> 
  );
}

export default Alert;

Alert.propTypes = {
  job: PropTypes.object.isRequired,
  onChangeHighPrice: PropTypes.func,
  onChangeLowPrice: PropTypes.func,
  onChangeMessage: PropTypes.func,
};