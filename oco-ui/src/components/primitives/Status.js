import React from 'react';
import { Icon } from 'semantic-ui-react';
import Span from './Span';

const Status = props => (
  <Span
      color={props.success ? 'success' : 'alert'}
      ml={2}
      fontSize={2}>
    {props.success &&
      <Icon name="info circle" />
    }
    {props.children &&
      <Icon name="warning circle" />
    }
    {props.children || (props.success && "Success")}
  </Span>
);

export default Status;