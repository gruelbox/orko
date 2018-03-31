import React from 'react';

import { Tab } from 'rebass';

const ForeTab = props => {
  if (props.selected) {
    return <Tab color="emphasis" borderColor="emphasis">{props.children}</Tab>;
  } else {
    return <Tab color="fore">{props.children}</Tab>;
  }
};

export default ForeTab;