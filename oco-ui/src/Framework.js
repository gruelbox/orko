import React from 'react';

import { Switch, Route, BrowserRouter } from 'react-router-dom';

import { Flex, Box } from 'rebass';
import { ThemeProvider } from 'styled-components';
import theme from './theme';

import ToolbarContainer from './containers/ToolbarContainer';
import AddCoinContainer from './containers/AddCoinContainer';
import Trading from './Trading';

// TEMP
import styled from 'styled-components';
import { space } from 'styled-system'

const BackgroundBox = styled.div`
  background-color: ${props => props.theme.colors.backgrounds[0]};
  height: 100vh;
  ${space}
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
            <Switch>
              <Route exact path='/addCoin'
                component={AddCoinContainer}/>
              <Route path='/coin/:exchange/:counter/:base'
                component={Trading}/>
              <Route component={Trading}/>
            </Switch>
          </BackgroundBox>
        </ThemeProvider>
      </BrowserRouter>
    );
  }
}