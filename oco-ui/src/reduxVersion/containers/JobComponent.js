import React, { Component } from 'react';
import { TickerConsumer } from './context/TickerContext';
import { Tab } from 'semantic-ui-react';
import SubmitLimitTrade from './submit/SubmitLimitTrade';
import SubmitStop from './submit/SubmitStop';
import SubmitAlert from './submit/SubmitAlert';
import { BUY, SELL } from './context/trade';

export default class Actions extends Component {
  render() {
    const coin = this.props.coin;
    return (
      <TickerConsumer>
        { ticker => {

          const setBidPrice = (setter) => setter(ticker.bid);
          const setAskPrice = (setter) => setter(ticker.ask);

          return (
            <Tab panes={[
              { menuItem: 'Buy', render: () => 
                <Tab.Pane>
                  <SubmitLimitTrade direction={BUY} coin={coin} marketPrice={ticker.ask} setBidPrice={setBidPrice} setAskPrice={setAskPrice} />
                </Tab.Pane>
              },
              { menuItem: 'Sell', render: () =>
                <Tab.Pane>
                  <SubmitLimitTrade direction={SELL} coin={coin} marketPrice={ticker.bid} setBidPrice={setBidPrice} setAskPrice={setAskPrice}/>
                </Tab.Pane>
              },
              { menuItem: 'Stop Buy', render: () =>
                <Tab.Pane>
                  <SubmitStop direction={BUY} coin={coin} marketPrice={ticker.bid} setBidPrice={setBidPrice} setAskPrice={setAskPrice}/>
                </Tab.Pane>
              },
              { menuItem: 'Stop Sell', render: () =>
                <Tab.Pane>
                  <SubmitStop direction={SELL} coin={coin} marketPrice={ticker.bid} setBidPrice={setBidPrice} setAskPrice={setAskPrice}/>
                </Tab.Pane>
              },
              { menuItem: 'Alert', render: () =>
                <Tab.Pane>
                  <SubmitAlert coin={coin} marketPrice={ticker.bid}/>
                </Tab.Pane>
              }
            ]} />
          );
        }}
      </TickerConsumer>
    );
  }
}