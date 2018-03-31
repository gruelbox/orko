import React from 'react';

import { Switch, Route, BrowserRouter } from 'react-router-dom';

import { Flex, Box } from 'rebass';
import { ThemeProvider } from 'styled-components';
import theme from './theme';

import CoinInfoContainer from './containers/CoinInfoContainer';
import ToolbarContainer from './containers/ToolbarContainer';
import CoinsContainer from './containers/CoinsContainer';
import AddCoinContainer from './containers/AddCoinContainer';
import MarketContainer from './containers/MarketContainer';

// TEMP
import styled from 'styled-components';
import { space, padding } from 'styled-system'

const BackgroundBox = styled.div`
  background-color: ${props => props.theme.colors.page};
  height: 100vh;
  ${space}
`;

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
  height: 100%;
  ${space}
  ${padding}
`;

const LightComponentBox = styled.div`
  background-color: ${props => props.theme.colors.box2};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  height: 100%;
  ${space}
  ${padding}
`;

export default class Framework extends React.Component {
  render() {
    return (
      <BrowserRouter>
        <ThemeProvider theme={theme}>
          <BackgroundBox>
            <Flex flexWrap='wrap'>
              <Box width={[1, 1]}>
                <ToolbarContainer />
              </Box>
            </Flex>
            <Flex flexWrap='wrap'>
              <Box width={[1, 170]} order={[1, 1]}>
                <LightComponentBox p={2}>
                  <CoinsContainer/>
                </LightComponentBox>
              </Box>
              <Box flex="1" order={[3, 2]}>
                <DarkComponentBox  p={2}>
                  <Switch>
                    <Route exact path='/addCoin'
                      component={AddCoinContainer}/>
                    <Route path='/coin/:exchange/:counter/:base'
                      component={CoinInfoContainer}/>
                  </Switch>
                </DarkComponentBox>
              </Box>
              <Box width={[1, 200]} order={[2, 3]}>
                <MidComponentBox  p={2}>
                  <MarketContainer />
                </MidComponentBox>
              </Box>
            </Flex>
          </BackgroundBox>
        </ThemeProvider>
      </BrowserRouter>
    );
  }
}