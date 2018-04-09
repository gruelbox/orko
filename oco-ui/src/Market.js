import React from 'react';

import { Box } from 'rebass';

import MarketContainer from './containers/MarketContainer';
import OpenOrdersContainer from './containers/OpenOrdersContainer';

import MidComponentBox from './components/primitives/MidComponentBox';
import LightComponentBox from './components/primitives/LightComponentBox';

import { coin as createCoin } from './store/coin/reducer';

export default class Trading extends React.Component {
  render() {

    const coin = this.props.match.params.exchange ? createCoin(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base
    ) : undefined;

    return (
      <Box width={[1, 320]} order={[2, 3]}>
        <LightComponentBox p={2}>
          <MarketContainer coin={coin} />
        </LightComponentBox>
        <MidComponentBox p={2}>
          <OpenOrdersContainer coin={coin}  />
        </MidComponentBox>
      </Box>
    );
  }
}