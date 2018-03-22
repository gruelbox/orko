import React, { Component } from 'react';
import { Input, Button, Form, Accordion } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import SubmitJob from './SubmitJob';
import RiskProfile from './RiskProfile';
import { BUY } from '../context/trade'

export default class SubmitLimitTrade extends Component {

  constructor(props) {
    super(props);
    this.state = {
      amount: undefined,
      limitPrice: undefined
    };
  }

  onChangeAmount = event => this.setState({
    amount: event.target.value
  });

  onSetPrice = (price) => this.setState({
    limitPrice: price
  });

  onChangePrice = event => this.onSetPrice(event.target.value);

  onSetMarketPrice = () => this.onSetPrice(this.props.marketPrice);

  render() {

    const isValidNumber = (val) => !isNaN(val) && val > 0 && val !== '';

    const limitPrice = this.state.limitPrice === undefined
      ? this.props.marketPrice
      : this.state.limitPrice

    const color = this.props.direction === BUY ? 'green' : 'red';

    const priceValid = isValidNumber(limitPrice);
    const amountValid = isValidNumber(this.state.amount);
    const valid = priceValid && amountValid;

    const job = {
      jobType: "LimitOrderJob",
      direction: this.props.direction,
      tickTrigger: {
        exchange: this.props.coin.exchange,
        counter: this.props.coin.counter,
        base: this.props.coin.base
      },
      bigDecimals: {
        amount: this.state.amount,
        limitPrice: limitPrice,
      }
    };

    return (
      <Form>

        <Form.Group widths='equal'>
          <Form.Field required>
            <label>Limit price</label>
            <Input type='text' placeholder="Enter limit price..." action error={!priceValid}>
              <input value={limitPrice} onChange={this.onChangePrice} />
              <Button color={color} onClick={() => this.props.setBidPrice(this.onSetPrice)}>Bid</Button>
              <Button color={color} onClick={() => this.props.setAskPrice(this.onSetPrice)}>Ask</Button>
              <Button color={color} onClick={this.onSetMarketPrice}>Market</Button>
            </Input>
          </Form.Field> 
          <Form.Input
            required
            label="Amount"
            type='text'
            placeholder='Enter amount...'
            value={this.state.amount || ''}
            onChange={this.onChangeAmount}
            error={!amountValid}
          />
        </Form.Group>

        { /* <Accordion exclusive={false} panels={[
          {
            title: "Advanced",
            content: (
              <div>
                <Form.Group inline>
                  <Form.Checkbox label='Stop at' />
                  <Form.Input type='text' placeholder='Price...'/>
                  <Form.Input label="Limit" type='text' placeholder='Price...'/>
                  <Form.Checkbox label='Trailing' />
                </Form.Group>
                <Form.Group inline>
                  <Form.Checkbox label='Take profit at' />
                  <Form.Input type='text' placeholder='Price...'/>
                  <Form.Checkbox label='Trailing' />
                </Form.Group>
              </div>
            )
          },
          {
            title: "Ratios",
            content: (
              <RiskProfile
                amount={this.state.amount}
                entryPrice={this.state.limitPrice}
              />
            )
          }
        ]}/> */}

        <SubmitJob job={job} valid={valid}/>

      </Form> 
    );
  }
}

SubmitLimitTrade.propTypes = {
  direction: PropTypes.string.isRequired,
  coin: PropTypes.object.isRequired,
  marketPrice: PropTypes.number.isRequired
};