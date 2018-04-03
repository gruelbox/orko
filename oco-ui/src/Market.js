import React from 'react';

import { Box } from 'rebass';

import MarketContainer from './containers/MarketContainer';
import OpenOrdersContainer from './containers/OpenOrdersContainer';

import MidComponentBox from './components/primitives/MidComponentBox';
import LightComponentBox from './components/primitives/LightComponentBox';

export default class Trading extends React.Component {
  render() {
    return (
      <Box width={[1, 200]} order={[2, 3]}>
        <LightComponentBox p={2}>
          <MarketContainer />
        </LightComponentBox>
        <MidComponentBox p={2}>
          <OpenOrdersContainer />
        </MidComponentBox>
      </Box>
    );
  }
}