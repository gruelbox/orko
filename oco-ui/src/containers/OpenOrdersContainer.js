import React from 'react';
import { connect } from 'react-redux';

import SectionHeading from '../components/primitives/SectionHeading';
import ForePara from '../components/primitives/ForePara';

const OpenOrdersContainer = props => (
  <div>
    <SectionHeading>Open Orders</SectionHeading>
    <ForePara>No market data</ForePara>
  </div>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(OpenOrdersContainer);