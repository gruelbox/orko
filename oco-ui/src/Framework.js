import React from 'react';

import { Switch, Route, BrowserRouter } from 'react-router-dom';

import { Flex, Box } from 'rebass';

import CoinsContainer from './containers/CoinsContainer';
import JobContainer from './containers/JobContainer';
import JobsContainer from './containers/JobsContainer';
import ToolbarContainer from './containers/ToolbarContainer';
import AddCoinContainer from './containers/AddCoinContainer';
import CoinInfoContainer from './containers/CoinInfoContainer';
import MarketContainer from './containers/MarketContainer';
import OpenOrdersContainer from './containers/OpenOrdersContainer';
import TradeSelector from './components/TradeSelector';
import Chart from './components/Chart';

import WithCoinParameter from './WithCoinParameter';

const MarketContainerProvider = props => <WithCoinParameter {...props} component={MarketContainer}/>;
const OpenOrdersContainerProvider = props => <WithCoinParameter {...props} component={OpenOrdersContainer}/>;
const CoinInfoContainerProvider = props => <WithCoinParameter {...props} component={CoinInfoContainer}/>;
const TradeSelectorProvider = props => <WithCoinParameter {...props} component={TradeSelector}/>;
const ChartProvider = props => <WithCoinParameter {...props} component={Chart}/>;

export default class Framework extends React.Component {
  render() {
    return (
      <BrowserRouter>
        <div>
          <Switch>
            <Route path='/coin/:exchange/:counter/:base' component={ToolbarContainer}/>
            <Route component={ToolbarContainer}/>
          </Switch>
          <Flex flexWrap='wrap' h='calc(100%-66px)'>
            <Box flex="1">
              <Switch>
                <Route exact path='/addCoin' component={AddCoinContainer}/>
                <Route path='/coin/:exchange/:counter/:base' component={CoinInfoContainerProvider}/>
                <Route path='/job/:jobId' component={JobContainer}/>
              </Switch>
              <Switch>
                <Route path='/coin/:exchange/:counter/:base' component={ChartProvider}/>
              </Switch>
              <Switch>
                <Route path='/coin/:exchange/:counter/:base' component={TradeSelectorProvider}/>
              </Switch>
            </Box>
            <Box width={[1, 450]}>
              <CoinsContainer/>
              <Switch>
                <Route path='/coin/:exchange/:counter/:base' component={MarketContainerProvider}/>
              </Switch>
              <Switch>
                <Route path='/coin/:exchange/:counter/:base' component={OpenOrdersContainerProvider}/>
              </Switch>
              <JobsContainer/>
            </Box>
          </Flex>
        </div>
      </BrowserRouter>
    );
  }
}