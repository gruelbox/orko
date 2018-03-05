import React, { Component } from 'react';
import { Icon, Input, Header, Select, Button, Form, Segment } from 'semantic-ui-react'
import './App.css';
import { connect } from 'react-redux';
import { tickerName } from './redux/ticker';

export const BUY = 'buy';
export const SELL = 'sell';

class SimpleTrade extends Component {

  render() {

    const color = this.props.direction === BUY ? 'green' : 'red';
    const description = this.props.direction === BUY ? 'Limit Buy' : 'Limit Sell';
    const buttonText = this.props.direction === BUY ? 'Buy' : 'Sell';

    var baseBalance = {
      available: 0,
      total: 0
    };
    var counterBalance = {
      available: 0,
      total: 0
    };

    if (this.props.balances && this.props.balances.get(this.props.exchange)) {
      const balances = this.props.balances.get(this.props.exchange);
      if (balances) {
        if (balances.get(this.props.base))
          baseBalance = balances.get(this.props.base);
        if (balances.get(this.props.counter))
          counterBalance = balances.get(this.props.counter);
      }
    }

    var ticker = this.props.tickers.get(tickerName(this.props));
    if (!ticker) {
      ticker = {
        ask: 0,
        bid: 0
      };
    }

    const price = this.props.direction === BUY ? ticker.ask : ticker.bid;
    
    return (
      <Segment basic loading={price === 0}>
        <Header as='h2'> 
          <Icon color={color} name='plus' />
          {description}
        </Header>
        <Form>
          <Form.Field>
            <label>Price</label>
            <Input type='text' placeholder='Enter price...' action>
              <input value={price} />
              <Button color={color} type='submit'>Market</Button>
            </Input>
          </Form.Field>
          <Form.Field>
            <label>Amount</label>
            <Input type='text' placeholder='Enter amount...' action>
              <input />
              <Select compact options={[
                { key: this.props.base, text: this.props.base + ' / ' + baseBalance.available, value: this.props.base }, 
                { key: this.props.counter, text: this.props.counter + ' / ' + counterBalance.available, value: this.props.base }
              ]} defaultValue={this.props.base} />
              <Button color={color} type='submit'>MAX</Button>
            </Input>
          </Form.Field>
          <Button color={color} type='submit'>{buttonText}</Button>
        </Form>  
      </Segment>
    )
  }
}

const mapStateToProps = state => ({
  balances: state.balances,
  tickers: state.tickers,
})

export default connect(mapStateToProps) (SimpleTrade);