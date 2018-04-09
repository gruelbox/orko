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

const BackgroundErrors = props => {
  const errors = props.errorBackground;
  const errorKeys = Object.keys(props.errorBackground);
  const hasErrors = errorKeys.length !== 0;
  const errorString = hasErrors ? errorKeys.map(k => errors[k]).join(", ") : null;
  return (
    <Span
      color={ hasErrors ? "red" : "black" }
      ml={4}
      fontWeight="bold"
    >
      <Icon
        name="wifi"
        color={ hasErrors ? "red" : "black" }
      />
      { hasErrors ? errorString : "Connected" }
    </Span>
  );
}

const HomeLink = props => (
  <Link to="/" color="black" fontWeight="bold">
    Home
  </Link>
)

const SignOutLink = props => (
  <Span ml="auto" color="black">
    <Href color='black' fontWeight="bold" onClick={props.onClick}>
      Sign out
    </Href>
    &nbsp;({props.userName})
  </Span>
)

const InvalidateLink = props => (
  <Href ml={4} color='black' fontWeight="bold" onClick={props.onClick}>
    Invalidate whitelist
  </Href>
)

const ToolbarContainer = props => (
  <ToolbarBox>
    <Toolbar>
      <HomeLink/>
      <BackgroundErrors errorBackground={props.errorBackground}/>
      <SignOutLink userName={props.userName} onClick={() => props.dispatch(authActions.logout())}/>
      <InvalidateLink onClick={() => props.dispatch(authActions.clearWhitelist()) } />
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