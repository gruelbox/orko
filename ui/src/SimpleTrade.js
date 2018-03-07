import React, { Component } from 'react';
import { Icon, Input, Header, Button, Form, Segment } from 'semantic-ui-react'
import './App.css';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { fetchBalances } from './redux/balance';
import { fetchTicker } from './redux/ticker';

export const BUY = 'buy';
export const SELL = 'sell';

class SimpleTrade extends Component {

  constructor(props) {
    super(props);
    this.state = {
      price: this._marketPrice(),
      amount: undefined
    };
  }

  _marketPrice = () => {
    return this.props.direction === BUY ? this._askPrice() : this._bidPrice();
  }

  _bidPrice = () => {
    const ticker = this.props.tickers.getTicker(this.props);
    return ticker ? ticker.bid : undefined;
  }

  _askPrice = () => {
    const ticker = this.props.tickers.getTicker(this.props);
    return ticker ? ticker.ask : undefined;
  }

  onChangePrice = event => this.setState({ price: event.target.value });
  onChangeAmount = event => this.setState({ amount: event.target.value });

  onSetAskPrice = () => this.setState({ price: this._askPrice() });
  onSetBidPrice = () => this.setState({ price: this._bidPrice() });
  onSetMaxAmount = () => {
    console.log("SimpleTrade: set max amount");
    var amount;
    if (this.props.direction === BUY) {
      amount = this.props.balances.get(this.props.exchange, this.props.counter).available / this.state.price;
    } else {
      amount = this.props.balances.get(this.props.exchange, this.props.base).available;
    }
    console.log("SimpleTrade: setting amount to ", amount);
    this.setState({ amount: amount });
  };

  render() {
    console.log("SimpleTrade: render");

    this.props.fetchBalances("binance", ["VEN", "BTC"]);
    this.props.fetchTicker("binance", "BTC", "VEN");

    const color = this.props.direction === BUY ? 'green' : 'red';
    const description = this.props.direction === BUY ? 'Limit Buy' : 'Limit Sell';
    const buttonText = this.props.direction === BUY ? 'Buy' : 'Sell';

    //var baseBalance = this.props.balances.get(this.props.exchange, this.props.base);
    //var counterBalance = this.props.balances.get(this.props.exchange, this.props.counter);

    var price = this.state.price ? this.state.price : this._marketPrice();

    return (
      <Segment loading={this.props.tickers === undefined} disabled={!this.props.auth.valid}>
        <Header as='h2'> 
          <Icon color={color} name='plus' />
          {description}
        </Header>
        <Form>
          <Form.Field>
            <label>Price</label>
            <Input type='text' placeholder='Enter price...' action>
              <input value={price || ''} onChange={this.onChangePrice} />
              <Button color={color} type='submit' onClick={this.onSetBidPrice}>Bid</Button>
              <Button color={color} type='submit' onClick={this.onSetAskPrice}>Ask</Button>
            </Input>
          </Form.Field>
          <Form.Field>
            <label>Amount</label>
            <Input type='text' placeholder='Enter amount...' action>
              <input value={this.state.amount || ''} onChange={this.onChangeAmount}/>
              <Button color={color} type='submit' onClick={this.onSetMaxAmount}>MAX</Button>
            </Input>
          </Form.Field>
          <Button color={color} type='submit'>{buttonText}</Button>
        </Form>  
      </Segment>
    )
  }
}

SimpleTrade.propTypes = {
  direction: PropTypes.string.isRequired,
  base: PropTypes.string.isRequired,
  counter: PropTypes.string.isRequired,
  onChange: PropTypes.func
};

SimpleTrade.defaultProps = {
  onChange: () => {}
};

const mapStateToProps = state => ({
  balances: state.balances,
  tickers: state.tickers,
  auth: state.auth,
});

const mapDispatchToProps = {
  fetchBalances: (exchange, currencies) => fetchBalances(exchange, currencies),
  fetchTicker: (exchange, counter, base) => fetchTicker(exchange, counter, base)
};

export default connect(mapStateToProps, mapDispatchToProps) (SimpleTrade);