import React, { Component } from 'react';
import { Form } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import SubmitJob from './SubmitJob';
import Alert from './Alert';

export default class SubmitAlert extends Component {

  constructor(props) {
    super(props);
    this.state = {
      highPrice: "",
      lowPrice: "",
      message: "Alert"
    };
  }

  onChangeHighPrice = event => this.setState({
    highPrice: event.target.value
  });

  onChangeLowPrice = event => this.setState({
    lowPrice: event.target.value
  });

  onChangeMessage = event => this.setState({
    message: event.target.value
  });

  render() {

    const isValidNumber = (val) => !isNaN(val) && val !== '' && val > 0;

    const highPriceValid = isValidNumber(this.state.highPrice);
    const lowPriceValid = isValidNumber(this.state.lowPrice);
    const valid = this.state.message !== "" && (highPriceValid || lowPriceValid);

    const tickTrigger = {
      exchange: this.props.coin.exchange,
      counter: this.props.coin.counter,
      base: this.props.coin.base
    };

    const job = {
      jobType: "OneCancelsOther",
      tickTrigger: tickTrigger,
      low: lowPriceValid ? {
          thresholdAsString: String(this.state.lowPrice),
          job: {
            jobType: "Alert",
            message: "Price of " + this.props.coin.name + " dropped below [" + this.state.lowPrice + "]: " + this.state.message
          }
      } : null,
      high: highPriceValid ? {
        thresholdAsString: String(this.state.highPrice),
        job: {
          jobType: "Alert",
          message: "Price of " + this.props.coin.name + " rose above [" + this.state.highPrice + "]: " + this.state.message
        }
      } : null
    }

    return (
      <Form>
        <Alert
          job={job}
          onChangeHighPrice={this.onChangeHighPrice}
          onChangeLowPrice={this.onChangeLowPrice}
          onChangeMessage={this.onChangeMessage}/>
        <SubmitJob job={job} valid={valid}/>
      </Form> 
    );
  }
}

SubmitAlert.propTypes = {
  coin: PropTypes.object.isRequired,
  marketPrice: PropTypes.number.isRequired
};