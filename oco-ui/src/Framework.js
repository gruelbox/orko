import React from 'react';

import KitchenSink from './KitchenSink';
import CoinInfoContainer from './containers/CoinInfoContainer';
import ToolbarContainer from './containers/ToolbarContainer';

import { Flex, Box, Toolbar, NavLink } from 'rebass';
import { ThemeProvider } from 'styled-components';

import { coin } from './store/coin/reducer';
import * as coinActions from './store/coin/actions';

// TEMP
import { connect } from 'react-redux';
import styled from 'styled-components';
import { space } from 'styled-system'

const BackgroundBox = styled.div`
  background-color: ${props => props.theme.ocoBackground}
  ${space}
`;

const MidComponentBox = styled.div`
  background-color: ${props => props.theme.ocoComponentBg1}
  ${space}
`;

const LightComponentBox = styled.div`
  background-color: ${props => props.theme.ocoComponentBg2}
  ${space}
`;

export class Framework extends React.Component {

  componentDidMount () {
    this.props.dispatch(coinActions.setCoin(coin("binance", "BTC", "VEN")));
  }

  render() {
    return (
      <ThemeProvider
        theme={{
          breakpoints: ['48em'],
          fontSizes: [12, 12],
          colors: {
            black: '#000',
            white: '#fff'
          },
          radii: [ 0, 0 ],
          ocoBackground: '#1E2B34',
          ocoComponentBg1: '#29353D',
          ocoComponentBg2: '#3A444D',
          ocoToolbarBg: 'white'
        }}>
        <BackgroundBox>
          <Flex flexWrap='wrap'>
            <Box width={[1, 1]}>
              <ToolbarContainer />
            </Box>
          </Flex>
          <Flex flexWrap='wrap'>
            <Box width={[1, 3/16]}>
              <MidComponentBox>Coins</MidComponentBox>
            </Box>
            <Box width={[1, 10/16]}>
              <CoinInfoContainer />
            </Box>
            <Box width={[1, 3/16]}>
              <LightComponentBox>Market</LightComponentBox>
            </Box>
          </Flex>
        </BackgroundBox>
      </ThemeProvider>
    );
  }
}

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(Framework);