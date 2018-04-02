import React from 'react';

import { Icon } from 'semantic-ui-react';

import Span from './Span';

const Warning = props => (
  <Span color="alert">
    <Icon name="warning sign" />
    <span>{props.children}</span>
  </Span>
);

export default Warning;