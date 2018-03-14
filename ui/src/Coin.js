import React, { Component } from 'react';
import { Subscribe  } from 'unstated';
import CoinContainer from './context/CoinContainer';
import { TickerConsumer, TickerProvider } from './context/TickerContext';
import AuthContainer from './context/AuthContainer';
import { Button, Header, Icon, Message, Tab, Grid, Statistic } from 'semantic-ui-react';
import { coin as coinDef } from './context/coin';
import Trade from './Trade';
import { BUY, SELL } from './context/trade';
import Chart from './Chart';

export default class Coin extends Component {

  constructor(props) {
    super(props);
    this.state = {
      showChart: false
    };
  }

  onToggleChart = () => {
    this.setState(old => ({ showChart: !old.showChart }));
  }

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
        <Subscribe to={[CoinContainer, AuthContainer]}>
          {(coinContainer, auth) => {

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

            return (
              <TickerProvider coin={coin}>
                <TickerConsumer>
                  { ticker => {

                    const setBidPrice = (setter) => setter(ticker.bid);
                    const setAskPrice = (setter) => setter(ticker.ask);

                    return (
                      <div>
                        <Grid>
                          <Grid.Row divided columns={3}>
                            <Grid.Column width={500}>
                              <Header as='h2'>
                                <Icon name='bitcoin' />
                                {coin.name}
                              </Header>
                            </Grid.Column>
                            <Grid.Column width={50}>
                              <Button onClick={this.onToggleChart} toggle={this.state.showChart}>
                                <Icon name="line chart"/>
                                Show chart
                              </Button>
                            </Grid.Column>
                            <Grid.Column width={50}>
                              <Button onClick={() => this.onRemove(coinContainer)}>Remove coin</Button>
                            </Grid.Column>
                          </Grid.Row>
                          <Grid.Row divided columns={4}>
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
                        {this.state.showChart &&
                          <div style={{marginTop: "1em"}}>
                            <Chart symbol={coin.base + coin.counter} />
                          </div>
                        }
                        <div style={{marginTop: "1em"}}>
                          <Tab panes={[
                            { menuItem: 'Buy', render: () => 
                              <Tab.Pane>
                                <Trade direction={BUY} coin={coin} marketPrice={ticker.ask} setBidPrice={setBidPrice} setAskPrice={setAskPrice} />
                              </Tab.Pane>
                            },
                            { menuItem: 'Sell', render: () =>
                              <Tab.Pane>
                                <Trade direction={SELL} coin={coin} marketPrice={ticker.bid} setBidPrice={setBidPrice} setAskPrice={setAskPrice}/>
                              </Tab.Pane>
                            },
                          ]} />
                        </div>
                      </div>
                    );
                  }}
                </TickerConsumer>
              </TickerProvider>
            );
          }}
      </Subscribe>
    );
  }
}