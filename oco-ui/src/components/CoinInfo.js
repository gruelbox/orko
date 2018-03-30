import React from 'react';
import { Button, Header, Icon, Grid } from 'semantic-ui-react';
import { coinShape, balanceShape, tickerShape } from '../store/coin/reducer';
import PropTypes from 'prop-types';
import CopyableNumber from './primitives/CopyableNumber';
import Chart from './Chart';

export const CoinInfoComponent = props => {
  const coin = props.coin;
  if (coin) {
    return (
      <div>
        <Grid>
          <Grid.Row divided columns={3}>
            <Grid.Column>
              <Header as='h3'>
                <Icon name='bitcoin' />
                {coin.name}
              </Header>
            </Grid.Column>
            <Grid.Column>
              <Button onClick={props.onToggleChart}>
                <Icon name="line chart"/>
                Show chart
              </Button>
            </Grid.Column>
            <Grid.Column>
              <Button onClick={props.onRemove}>Remove coin</Button>
            </Grid.Column>
          </Grid.Row>
          <Grid.Row divided columns={3}>
            <Grid.Column>
              {(!props.balance)
                ? <div>
                    <Icon name="warning sign" />
                    Cannot fetch balance
                  </div>
                : <div>
                    <CopyableNumber label="Available"
                      onClick={props.onClickNumber}
                      number={props.balance.available}/>
                    <br/>
                    <CopyableNumber
                      label="Total balance"
                      onClick={props.onClickNumber}
                      number={props.balance.total}/>
                  </div>
              }
            </Grid.Column>
            <Grid.Column>
              {(!props.ticker)
                ? <div>
                    <Icon name="warning sign" />
                    Cannot fetch ticker
                  </div>
                : <div>
                    <CopyableNumber
                      label="Bid"
                      onClick={props.onClickNumber}
                      number={props.ticker.bid} />
                    <br/>
                    <CopyableNumber
                      label="Last"
                      onClick={props.onClickNumber}
                      number={props.ticker.last} />
                    <br/>
                    <CopyableNumber
                      label="Ask"
                      onClick={props.onClickNumber}
                      number={props.ticker.ask} />
                  </div>
              }
            </Grid.Column>
            <Grid.Column>
              {(!props.ticker)
                ? <div>
                    <Icon name="warning sign" />
                    Cannot fetch ticker
                  </div>
                : <div>
                    <CopyableNumber
                      label="High"
                      onClick={props.onClickNumber}
                      number={props.ticker.high} />
                    <br/>
                    <CopyableNumber
                      label="Open"
                      onClick={props.onClickNumber}
                      number={props.ticker.open} />
                    <br/>
                    <CopyableNumber
                      label="Low"
                      onClick={props.onClickNumber}
                      number={props.ticker.low} />
                  </div>
              }
            </Grid.Column>
          </Grid.Row>
        </Grid>
        <Chart coin={coin}/>
      </div>
    );
  } else {
    return <div>No coin selected</div>;
  }
};

export default CoinInfoComponent;

CoinInfoComponent.propTypes = {
  coin: PropTypes.shape(coinShape),
  balance: PropTypes.shape(balanceShape),
  ticker: PropTypes.shape(tickerShape),
  onToggleChart: PropTypes.func,
  onRemove: PropTypes.func,
  onClickNumber: PropTypes.func
};