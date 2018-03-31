import React from 'react';

import { Flex, Box } from 'rebass';

import Chart from './components/Chart';
import TradeSelector from './components/TradeSelector';
import CoinInfoContainer from './containers/CoinInfoContainer';
import CoinsContainer from './containers/CoinsContainer';
import MarketContainer from './containers/MarketContainer';
import OpenOrdersContainer from './containers/OpenOrdersContainer';
import JobsContainer from './containers/JobsContainer';

import { coin } from './store/coin/reducer';

import styled from 'styled-components';
import { space, padding } from 'styled-system'

const DarkComponentBox = styled.div`
  background-color: ${props => props.theme.colors.box3};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  height: 100%;
  ${space}
  ${padding}
`;

const MidComponentBox = styled.div`
  background-color: ${props => props.theme.colors.box1};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  height: 50%;
  ${space}
  ${padding}
`;

const LightComponentBox = styled.div`
  background-color: ${props => props.theme.colors.box2};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  height: 50%;
  ${space}
  ${padding}
`;

export default class Trading extends React.Component {

  constructor(props) {
    super(props);
    this.coin = coin(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base
    );
  }

  render() {
    return (
      <Flex flexWrap='wrap'>
        <Box width={[1, 170]} order={[1, 1]}>
          <LightComponentBox p={2}>
            <CoinsContainer/>
          </LightComponentBox>
          <LightComponentBox p={2}>
            <JobsContainer/>
          </LightComponentBox>
        </Box>
        <Box flex="1" order={[3, 2]}>
          <DarkComponentBox p={2}>
            <CoinInfoContainer coin={this.coin} />
            <div style={{height: '500px'}}><Chart coin={this.coin} /></div>
            <TradeSelector />
          </DarkComponentBox>
        </Box>
        <Box width={[1, 200]} order={[2, 3]}>
          <MidComponentBox p={2}>
            <MarketContainer />
          </MidComponentBox>
          <MidComponentBox p={2}>
            <OpenOrdersContainer />
          </MidComponentBox>
        </Box>
      </Flex>
    );
  }
}