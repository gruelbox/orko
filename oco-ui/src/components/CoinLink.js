import React from 'react';
import { Icon } from 'semantic-ui-react';

import ForeHref from './primitives/ForeHref';
import ForeLink from './primitives/ForeLink';

const CoinLink = props => (
  <div>
    <ForeHref onClick={props.onRemove}>
      <Icon name="close"/>
    </ForeHref>
    <ForeLink to={'/coin/' + props.coin.key}>
      {props.coin.name}
    </ForeLink>
  </div>
);

export default CoinLink;