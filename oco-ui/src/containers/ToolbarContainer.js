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
      <Span color={ props.errorBackground === null ? "black" : "red" } ml={4} fontWeight="bold">
        <Icon name="wifi" color={ props.errorBackground === null ? "black" : "red" }/>
        { props.errorBackground === null ? "OK" : props.errorBackground }
      </Span>
      <Span ml="auto" color="black">
        <Href color='black' fontWeight="bold" onClick={() => props.dispatch(authActions.logout()) }>
          Sign out
        </Href>
        &nbsp;({props.userName})
      </Span>
      <Href ml={4} color='black' fontWeight="bold" onClick={() => props.dispatch(authActions.clearWhitelist()) }>
        Invalidate whitelist
      </Href>
    </Toolbar>
  </ToolbarBox>
);

function mapStateToProps(state) {
  return {
    errorBackground: state.error.errorBackground,
    userName: state.auth.userName
  };
}

export default connect(mapStateToProps)(ToolbarContainer);