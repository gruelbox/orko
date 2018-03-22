import React, { Component } from 'react';
import { Form } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import SubmitJob from './SubmitJob';
import { BUY } from '../context/trade';

export default class SubmitStop extends Component {

  constructor(props) {
    super(props);
    this.state = {
      amount: "",
      stopPrice: "",
      limitPrice: undefined,
      trailing: false
    };
  }

  onChangeStopPrice = event => this.setState({
    stopPrice: event.target.value
  });

  onChangeLimitPrice = event => this.setState({
    limitPrice: event.target.value
  });

  onChangeAmount = event => this.setState({
    amount: event.target.value
  });

  onChangeTrailing = () => this.setState(prev => ({
    trailing: !prev.trailing
  }));

  render() {

    const isValidNumber = (val) => !isNaN(val) && val !== '';

    const limitPrice = this.state.limitPrice
      ? this.state.limitPrice
      : (this.props.direction === BUY ? String(this.state.stopPrice * 2) : "0");

    const createJob = () => {

      const tickTrigger = {
        exchange: this.props.coin.exchange,
        counter: this.props.coin.counter,
        base: this.props.coin.base
      };

      if (this.state.trailing) {
        return {
          jobType: "SoftTrailingStop",
          direction: this.props.direction,
          tickTrigger: tickTrigger,
          bigDecimals: {
            amount: String(this.state.amount),
            stopPrice: String(this.state.stopPrice),
            limitPrice: String(limitPrice),
            startPrice: String(this.props.marketPrice),
            lastSyncPrice: String(this.props.marketPrice)
          }
        };
      } else {
        const threshold = this.props.direction === BUY
          ? "high"
          : "low";
  
        // A non-trailing stop is just an OCO with only one side set.
        return {
          jobType: "OneCancelsOther",
          tickTrigger: tickTrigger,
          [threshold]: {
              thresholdAsString: String(this.state.stopPrice),
              job: {
                  jobType: "LimitOrderJob",
                  direction: this.props.direction,
                  tickTrigger: tickTrigger,
                  bigDecimals: {
                    amount: String(this.state.amount),
                    limitPrice: String(limitPrice)
                  }
              }
          }
        }
      }
    };

    const amountValid = isValidNumber(this.state.amount) && this.state.amount > 0;
    const stopPriceValid = isValidNumber(this.state.stopPrice) && this.state.stopPrice > 0;
    const limitPriceValid = isValidNumber(limitPrice) && limitPrice >= 0
    const valid = amountValid && stopPriceValid && limitPriceValid;

    return (
      <Form>

        <Form.Input
          error={!amountValid}
          label="Amount"
          type='text'
          placeholder='Enter amount...'
          value={this.state.amount || ''}
          onChange={this.onChangeAmount}
        />

        <Form.Input
          error={!stopPriceValid}
          label="Stop price"
          type='text'
          placeholder='Enter amount...'
          value={this.state.stopPrice || ''}
          onChange={this.onChangeStopPrice}
        />

        <Form.Input
          label="Limit price"
          type='text'
          placeholder='Defaults to no limit'
          value={this.state.limitPrice || ''}
          onChange={this.onChangeLimitPrice}
        />
        
        <Form.Checkbox label='Trailing' checked={this.state.trailing} onChange={this.onChangeTrailing}/>

        <SubmitJob job={createJob()} valid={valid}/>

      </Form> 
    );
  }
}

SubmitStop.propTypes = {
  direction: PropTypes.string.isRequired,
  coin: PropTypes.object.isRequired,
  marketPrice: PropTypes.number.isRequired
};