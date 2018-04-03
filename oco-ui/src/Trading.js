import React from 'react';

import { Box } from 'rebass';

import Chart from './components/Chart';
import TradeSelector from './components/TradeSelector';
import CoinInfoContainer from './containers/CoinInfoContainer';

import Section from './components/primitives/Section';
import Para from './components/primitives/Para';
import DarkComponentBox from './components/primitives/DarkComponentBox';
import MidComponentBox from './components/primitives/MidComponentBox';

import { coin as createCoin } from './store/coin/reducer';

export default class Trading extends React.Component {

  render() {

    const coin = this.props.match.params.exchange ? createCoin(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base
    ) : undefined;

    const showChart = false;

    return (
      <Box flex="1" order={[1, 2]}>
        {coin && (
          <DarkComponentBox p={2}>
            <CoinInfoContainer coin={coin} />
            {showChart &&
              <div style={{height: '500px'}}>
                <Chart coin={coin} />
              </div>
            }
          </DarkComponentBox>
        )}
        <MidComponentBox p={2}>
          <Section heading="Trading">
            {!coin && (
              <Para>No coin selected</Para>
            )}
            {coin && (
              <TradeSelector coin={coin}/>
            )}
          </Section>
        </MidComponentBox>
      </Box>
    );
  }
}