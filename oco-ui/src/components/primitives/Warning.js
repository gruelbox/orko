import React from 'react';

import { Icon } from 'semantic-ui-react';

import ForeSpan from './ForeSpan';

const Warning = props => (
  <ForeSpan>
    <Icon name="warning sign" />
    <span>{props.children}</span>
  </ForeSpan>
);

export default ForeSpan;