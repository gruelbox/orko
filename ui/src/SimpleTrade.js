import React, { Component } from 'react';
import { Icon, Input, Header, Select, Button, Form, Segment } from 'semantic-ui-react'
import './App.css';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

export const BUY = 'buy';
export const SELL = 'sell';

class SimpleTrade extends Component {

  render() {

    const color = this.props.direction === BUY ? 'green' : 'red';
    const description = this.props.direction === BUY ? 'Limit Buy' : 'Limit Sell';
    const buttonText = this.props.direction === BUY ? 'Buy' : 'Sell';

    var baseBalance = this.props.balances.get(this.props.exchange, this.props.base);
    var counterBalance = this.props.balances.get(this.props.exchange, this.props.counter);
    var ticker = this.props.tickers.get(this.props);

    const price = this.props.direction === BUY ? ticker.ask : ticker.bid;
    
    return (
      <Segment loading={price === 0} disabled={!this.props.auth.valid}>
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

SimpleTrade.propTypes = {
  direction: PropTypes.string.isRequired,
  base: PropTypes.string.isRequired,
  counter: PropTypes.string.isRequired
};

const mapStateToProps = state => ({
  balances: state.balances,
  tickers: state.tickers,
  auth: state.auth,
});

export default connect(mapStateToProps) (SimpleTrade);