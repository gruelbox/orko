import React from 'react';
import { connect } from 'react-redux';

import styled from 'styled-components';
import { space } from 'styled-system';

import { Toolbar } from 'rebass';
import { Icon } from 'semantic-ui-react';
import Link from '../components/primitives/Link';
import Href from '../components/primitives/Href';
import Span from '../components/primitives/Span';

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
      <Span to="/" color={ props.errorBackground === null ? "black" : "red" } ml={4} fontWeight="bold">
        <Icon name="wifi" color={ props.errorBackground === null ? "black" : "red" }/>
        { props.errorBackground === null ? "OK" : props.errorBackground }
      </Span>
      <Href color='black' ml="auto" fontWeight="bold" onClick={() => props.dispatch(authActions.logout())}>
        Sign out
      </Href>
    </Toolbar>
  </ToolbarBox>
);

function mapStateToProps(state) {
  return {
    errorBackground: state.error.errorBackground
  };
}

export default connect(mapStateToProps)(ToolbarContainer);