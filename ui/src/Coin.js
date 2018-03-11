import React, { Component } from 'react';
import { Subscribe  } from 'unstated';
import { CoinContainer } from './context/CoinContainer';
import { TickerContainer } from './context/TickerContainer';
import { AuthContainer } from './context/AuthContainer';
import { Button, Header, Icon, Message, Tab, Grid, Statistic } from 'semantic-ui-react';
import { coin as coinDef } from './context/coin';
import Chart from './Chart';


export default class Coin extends Component {

  onRemove = (coinContainer) => {
    var t = coinDef(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base,
    );
    coinContainer.removeCoin(t);
    this.props.history.push('/');
  };

  render() {

    var coin = coinDef(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base,
    );

    return (
      <div>
        <Subscribe to={[CoinContainer, TickerContainer, AuthContainer]}>
          {(coinContainer, tickerContainer, auth) => {

            if (!coinContainer.hasCoin(coin)) {
              return (
                <Message>
                  <Message.Header>
                    Unregistered coin
                  </Message.Header>
                  <p>Make sure you add the coin first.</p>
                </Message>
              );
            }

            const ticker = tickerContainer.getTicker(coin, auth);

            return (
              <Grid divided>
                <Grid.Row columns={2}>
                  <Grid.Column>
                    <Header as='h2'>
                      <Icon name='bitcoin' />
                      {coin.name}
                    </Header>
                  </Grid.Column>
                  <Grid.Column>
                    <Button onClick={() => this.onRemove(coinContainer)}>Remove coin</Button>
                  </Grid.Column>
                </Grid.Row>
                <Grid.Row columns={4}>
                  <Grid.Column>
                    <Statistic size="tiny">
                      <Statistic.Value>{ticker.last}</Statistic.Value>
                      <Statistic.Label>{coin.counter}</Statistic.Label>
                    </Statistic>
                  </Grid.Column>
                  <Grid.Column>
                    {'Bid: ' + ticker.bid}
                    <br/>
                    {'Last: ' + ticker.last}
                    <br/>
                    {'Ask: ' + ticker.ask}
                  </Grid.Column>
                  <Grid.Column>
                    {'High: ' + ticker.high}
                    <br/>
                    {'Open: ' + ticker.open}
                    <br/>
                    {'Low: ' + ticker.low}
                  </Grid.Column>
                  <Grid.Column>
                    {'Vol: ' + ticker.volume}
                  </Grid.Column>
                </Grid.Row>
              </Grid>
            );
          }}
        </Subscribe>
        <div style={{marginTop: "1em"}}>
          <Chart symbol={coin.base + coin.counter} />
        </div>
        <div style={{marginTop: "1em"}}>
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
      </div>
    );
  }
}