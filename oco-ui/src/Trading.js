import React from 'react';

import { Flex, Box } from 'rebass';

import Chart from './components/Chart';
import TradeSelector from './components/TradeSelector';
import CoinInfoContainer from './containers/CoinInfoContainer';
import CoinsContainer from './containers/CoinsContainer';
import MarketContainer from './containers/MarketContainer';
import OpenOrdersContainer from './containers/OpenOrdersContainer';
import JobsContainer from './containers/JobsContainer';

import Section from './components/primitives/Section';
import Para from './components/primitives/Para';

import { coin as createCoin } from './store/coin/reducer';

import styled from 'styled-components';
import { space, padding } from 'styled-system'

const DarkComponentBox = styled.div.attrs({
  pb: 3
})`
  background-color: ${props => props.theme.colors.backgrounds[1]};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  ${space}
  ${padding}
`;

const MidComponentBox = styled.div.attrs({
  pb: 3
})`
  background-color: ${props => props.theme.colors.backgrounds[2]};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  ${space}
  ${padding}
`;

const LightComponentBox = styled.div.attrs({
  pb: 3
})`
  background-color: ${props => props.theme.colors.backgrounds[3]};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  ${space}
  ${padding}
`;

export default class Trading extends React.Component {

  render() {

    const coin = this.props.match.params.exchange ? createCoin(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base
    ) : undefined;

    const showChart = false;

    return (
      <Flex flexWrap='wrap' h='calc(100%-66px)'>
        <Box width={[1, 170]} order={[3, 1]}>
          <LightComponentBox p={2}>
            <CoinsContainer/>
          </LightComponentBox>
          <MidComponentBox p={2}>
            <JobsContainer/>
          </MidComponentBox>
        </Box>
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
        <Box width={[1, 200]} order={[2, 3]}>
          <LightComponentBox p={2}>
            <MarketContainer />
          </LightComponentBox>
          <MidComponentBox p={2}>
            <OpenOrdersContainer />
          </MidComponentBox>
        </Box>
      </Flex>
    );
  }
}