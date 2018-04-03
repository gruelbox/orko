import React from 'react';
import { connect } from 'react-redux';

import styled from 'styled-components';
import { space } from 'styled-system';

import { Toolbar } from 'rebass';
import Link from '../components/primitives/Link';
import Href from '../components/primitives/Href';

import * as authActions from '../store/auth/actions';

const ToolbarBox = styled.div`
  background-color: ${props => props.theme.colors.toolbar}
  ${space}
`;

const ToolbarContainer = props => (
  <ToolbarBox>
    <Toolbar>
      <Link to="/" color="black" fontWeight="bold">
        Home
      </Link>
      <Href color='black' ml='auto' fontWeight="bold" onClick={() => props.dispatch(authActions.logout())}>
        Sign out
      </Href>
    </Toolbar>
  </ToolbarBox>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(ToolbarContainer);