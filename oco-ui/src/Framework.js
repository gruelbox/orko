import React from 'react';

import CoinInfoContainer from './containers/CoinInfoContainer';
import ToolbarContainer from './containers/ToolbarContainer';
import CoinsContainer from './containers/CoinsContainer';

import { Flex, Box, Toolbar, NavLink } from 'rebass';
import { ThemeProvider } from 'styled-components';
import theme from './theme';

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
      <ThemeProvider theme={theme}>
        <BackgroundBox>
          <Flex flexWrap='wrap'>
            <Box width={[1, 1]}>
              <ToolbarContainer />
            </Box>
          </Flex>
          <Flex flexWrap='wrap'>
            <Box width={[1, 3/16]}>
              <CoinsContainer/>
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