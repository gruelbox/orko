import React, { Component } from 'react';
import { Input, Button, Form, Message, Step, Divider } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import { Subscribe } from 'unstated';
import AuthContainer from './context/AuthContainer';
import { executeTrade, createLimitOrder, BUY, SELL } from './context/trade'

export default class Trade extends Component {

  constructor(props) {
    super(props);
    this.state = {
      price: undefined,
      amount: undefined,
      executedTrade: undefined,
      processing: false,
      error: undefined
    };
  }

  onChangePrice = event => this.setState({
    price: event.target.value
  });

  onChangeAmount = event => this.setState({
    amount: event.target.value
  });

  onSetMarketPrice = (tickerContainer, auth) => this.setState({
    price: undefined
  });

  onSetPrice = (price) => this.setState({
    price: price
  });

  onTrade = (auth) => {
    this.setState({processing: true });
    executeTrade(
      createLimitOrder(
        this.props.coin,
        this.props.direction,
        this.state.price ? this.state.price : this.props.marketPrice,
        this.state.amount
      ),
      auth
    ).then(executedTrade => {
      this.setState({
        executedTrade: executedTrade,
        processing: false
      });
      setTimeout(() => this.setState({ executedTrade: undefined }), 5000);
    }).catch(e => {
      this.setState({
        processing: false,
        error: e
      });
      setTimeout(() => this.setState({ error: undefined }), 5000);
    });
  }

  /* onSetMaxAmount = () => {
    console.log("SimpleTrade: set max amount");
    var amount;
    if (this.props.direction === BUY) {
      amount = this.props.balances.get(this.props.exchange, this.props.counter).available / this.state.price;
    } else {
      amount = this.props.balances.get(this.props.exchange, this.props.base).available;
    }
    console.log("SimpleTrade: setting amount to ", amount);
    this.setState({ amount: amount });
  }; */

  render() {

    const color = this.props.direction === BUY ? 'green' : 'red';
    const buttonText = this.props.direction === BUY ? 'Buy' : 'Sell';

    //var baseBalance = this.props.balances.get(this.props.exchange, this.props.base);
    //var counterBalance = this.props.balances.get(this.props.exchange, this.props.counter);

    const valid = this.state.amount !== undefined && this.state.amount !== 0 && this.state.amount !== '';

    return (
      <Form attached>
        <Form.Field>
          <label>Price</label>
          <Input type='text' placeholder={'Market (' + this.props.marketPrice + ')'} action>
            <input value={this.state.price || ''} onChange={this.onChangePrice} />
            <Button color={color} onClick={() => this.props.setBidPrice(this.onSetPrice)}>Bid</Button>
            <Button color={color} onClick={() => this.props.setAskPrice(this.onSetPrice)}>Ask</Button>
            <Button color={color} onClick={this.onSetMarketPrice}>Market</Button>
          </Input>
        </Form.Field>
        <Form.Field>
          <label>Amount</label>
          <Input type='text' placeholder='Enter amount...' action>
            <input value={this.state.amount || ''} onChange={this.onChangeAmount}/>
            {/* <Button color={color} type='submit' onClick={this.onSetMaxAmount}>MAX</Button> */}
          </Input>
        </Form.Field>
        <Subscribe to={[AuthContainer]}>
          {(auth) => 
            <Button
              disabled={this.state.processing || !valid}
              color={color}
              type='submit'
              onClick={() => this.onTrade(auth)}>
                {buttonText}
            </Button>
          }
        </Subscribe>
        <Divider />
        <Step.Group ordered unstackable size="tiny" attached='bottom'>
          <Step active completed={valid}>
            <Step.Content>
              <Step.Title>Preparing</Step.Title>
              <Step.Description>Set up the order</Step.Description>
            </Step.Content>
          </Step>
          <Step active completed={this.state.processing || this.state.executedTrade || this.state.error}>
            <Step.Content>
              <Step.Title>Requested</Step.Title>
              <Step.Description>Submit to server</Step.Description>
            </Step.Content>
          </Step>
          <Step active completed={this.state.executedTrade !== undefined}>
            <Step.Content>
              <Step.Title>Processing</Step.Title>
              <Step.Description>Server accepted request</Step.Description>
            </Step.Content>
          </Step>
          <Step active>
            <Step.Content>
              <Step.Title>Complete</Step.Title>
              <Step.Description>Not checking yet</Step.Description>
            </Step.Content>
          </Step>
        </Step.Group>
        {this.state.error &&
          <Message>
            <Message.Header>
              Trade failed
            </Message.Header>
            <p>{this.state.error.message}</p>
          </Message>
        }
      </Form> 
    );
  }
}

Trade.propTypes = {
  direction: PropTypes.string.isRequired,
  coin: PropTypes.object.isRequired,
  initialPrice: PropTypes.number.isRequired,
  initialAmount: PropTypes.number.isRequired,
};