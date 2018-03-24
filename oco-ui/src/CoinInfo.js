import React, { Component } from 'react';
import { TickerConsumer } from './context/TickerContext';
import { Button, Header, Icon, Grid } from 'semantic-ui-react';
import { get } from './context/fetchUtil'
import { Subscribe  } from 'unstated';
import AuthContainer from './context/AuthContainer';

// TODO hacky....
const TICK_TIME = 20000;

export default class CoinInfo extends Component {
  
  constructor() {
    super();
    this.state = { };
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  start = (auth) => {
    this.interval = setInterval(() => this.tick(auth), TICK_TIME);
  }

  tick = (auth) => {

    if (!auth.isValid())
      return;

    const coin = this.props.coin;

    get('exchanges/' + coin.exchange + '/balance/' + coin.base, auth.getUserName(), auth.getPassword())
      .then(response => response.json())
      .then(json => {
        this.setState({balance: json[coin.base]});
      })
      .catch((error) => {
        console.log('Error fetching balance: ', error);
        this.setState({balance: undefined});
      });
  };

  render() {

    const coin = this.props.coin;
    return (
      <div>
        <Subscribe to={[AuthContainer]}>
          {(auth) => {
            if (!this.interval) {
              this.tick(auth);
              this.start(auth);
            }
            return <div/>;
          }}
        </Subscribe>
        <TickerConsumer>
          { ticker => (
              <Grid>
                <Grid.Row divided columns={3}>
                  <Grid.Column>
                    <Header as='h3'>
                      <Icon name='bitcoin' />
                      {coin.name}
                    </Header>
                  </Grid.Column>
                  <Grid.Column>
                    <Button onClick={this.props.onToggleChart}>
                      <Icon name="line chart"/>
                      Show chart
                    </Button>
                  </Grid.Column>
                  <Grid.Column>
                    <Button onClick={this.props.onRemove}>Remove coin</Button>
                  </Grid.Column>
                </Grid.Row>
                <Grid.Row divided columns={3}>
                  <Grid.Column>
                    {
                      (!this.state.balance)
                      ? <div>
                          <Icon name="warning sign" />
                          Cannot fetch balance
                        </div>
                      : <div>
                          {'Available: ' + this.state.balance.available}
                          <br/>
                          {'Total balance: ' + this.state.balance.total}
                        </div>
                    }
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
                </Grid.Row>
              </Grid>
          )}
        </TickerConsumer>
      </div>
    );
  }
}