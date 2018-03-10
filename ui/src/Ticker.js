import React, { Component } from 'react';
import { Subscribe  } from 'unstated';
import { TickerContainer } from './context/TickerContainer';
import { Button, Header, Icon, Message, Divider, Segment } from 'semantic-ui-react';
import { ticker } from './context/ticker';
import TradingViewWidget from 'react-tradingview-widget';


export default class Ticker extends Component {

  onRemove = (tickerContainer) => {
    var t = ticker(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base,
    );
    tickerContainer.removeTicker(t);
    this.props.history.push('/');
  };

  render() {
    return (
      <Subscribe to={[TickerContainer]}>
        {(tickerContainer) => {

          var t = ticker(
            this.props.match.params.exchange,
            this.props.match.params.counter,
            this.props.match.params.base,
          );
          if (!tickerContainer.hasTicker(t)) {
            return (
              <Message>
                <Message.Header>
                  Unregistered ticker
                </Message.Header>
                <p>Make sure you add the ticker first.</p>
              </Message>
            );
          }

          return <div>
            <Header as='h2'>
              <Icon name='bitcoin' />
              {t.name}
            </Header>
            <Button onClick={() => this.onRemove(tickerContainer)}>Remove this ticker</Button>
            <Divider />
            <div style={{height: 500}}>
              <TradingViewWidget 
                symbol={t.base + t.counter}
                hide_side_toolbar={false}
                autosize
                interval="240"
                allow_symbol_change={false}
                studies={['RSI@tv-basicstudies']}
              />
            </div>
          </div>
        }}
      </Subscribe>
    );
  }
}