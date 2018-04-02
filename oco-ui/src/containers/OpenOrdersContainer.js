import React from 'react';
import { connect } from 'react-redux';

import SectionHeading from '../components/primitives/SectionHeading';
import Para from '../components/primitives/Para';
import Panel from '../components/primitives/Panel';

const OpenOrdersContainer = props => (
  <div>
    <SectionHeading>Open Orders</SectionHeading>
    <Panel>
      <Para>No market data</Para>
    </Panel>
  </div>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(OpenOrdersContainer);