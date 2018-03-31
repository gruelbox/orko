import React from 'react';
import { connect } from 'react-redux';

import styled from 'styled-components';
import { space } from 'styled-system';

import { Toolbar, NavLink } from 'rebass';

import * as authActions from '../store/auth/actions';

const ToolbarBox = styled.div`
  background-color: ${props => props.theme.colors.toolbar}
  ${space}
`;

const ToolbarContainer = props => (
  <ToolbarBox>
    <Toolbar>
      <NavLink color='black' ml='auto' onClick={() => props.dispatch(authActions.logout())}>
        Sign out
      </NavLink>
    </Toolbar>
  </ToolbarBox>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(ToolbarContainer);