import React from 'react';

import { Tab as RebassTab } from 'rebass';

const Tab = props => {
  if (props.selected) {
    return <RebassTab color="emphasis" borderColor="emphasis">{props.children}</RebassTab>;
  } else {
    return <RebassTab color="fore">{props.children}</RebassTab>;
  }
};

export default Tab;