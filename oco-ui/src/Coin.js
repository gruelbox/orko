import React, { Component } from 'react';
import { Subscribe  } from 'unstated';
import CoinContainer from './context/CoinContainer';
import { TickerProvider } from './context/TickerContext';
import AuthContainer from './context/AuthContainer';
import { Message } from 'semantic-ui-react';
import { coin as coinDef } from './context/coin';
import Actions from './Actions';
import CoinInfo from './CoinInfo';
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
                <CoinInfo
                  coin={coin}
                  onToggleChart={this.onToggleChart}
                  onRemove={() => this.onRemove(coinContainer)} />
                {this.state.showChart &&
                  <div style={{marginTop: "1em"}}>
                    <Chart coin={coin} />
                  </div>
                }
                <div style={{marginTop: "1em"}}>
                  <Actions coin={coin}/>
                </div>
              </TickerProvider>
            );
          }}
      </Subscribe>
    );
  }
}