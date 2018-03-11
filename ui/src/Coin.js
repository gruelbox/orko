import React, { Component } from 'react';
import { Subscribe  } from 'unstated';
import { CoinContainer } from './context/CoinContainer';
import { Button, Header, Icon, Message, Divider, Segment, Tab } from 'semantic-ui-react';
import { coin } from './context/coin';
import TradingViewWidget from 'react-tradingview-widget';


export default class Coin extends Component {

  onRemove = (coinContainer) => {
    var t = coin(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base,
    );
    coinContainer.removeCoin(t);
    this.props.history.push('/');
  };

  render() {
    return (
      <Subscribe to={[CoinContainer]}>
        {(coinContainer) => {

          var t = coin(
            this.props.match.params.exchange,
            this.props.match.params.counter,
            this.props.match.params.base,
          );
          if (!coinContainer.hasCoin(t)) {
            return (
              <Message>
                <Message.Header>
                  Unregistered coin
                </Message.Header>
                <p>Make sure you add the coin first.</p>
              </Message>
            );
          }

          return <div>
            <Header as='h2'>
              <Icon name='bitcoin' />
              {t.name}
            </Header>
            <Button onClick={() => this.onRemove(coinContainer)}>Remove coin</Button>
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
            <Tab panes={[
              { menuItem: 'Buy', render: () => 
                <Tab.Pane>

                </Tab.Pane>
              },
              { menuItem: 'Sell', render: () =>
                <Tab.Pane>
                  
                </Tab.Pane>
              },
            ]} />
          </div>
        }}
      </Subscribe>
    );
  }
}