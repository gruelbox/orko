import React from 'react';
import { connect } from 'react-redux';

import Section from '../components/primitives/Section';
import Para from '../components/primitives/Para';
import Panel from '../components/primitives/Panel';

const OpenOrdersContainer = props => (
  <Section heading="Open Orders">
    <Panel>
      <Para>No market data</Para>
    </Panel>
  </Section>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(OpenOrdersContainer);