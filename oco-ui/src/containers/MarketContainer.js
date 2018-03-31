import React from 'react';
import { connect } from 'react-redux';

import SectionHeading from '../components/primitives/SectionHeading';

const MarketContainer = props => (
  <div>
    <SectionHeading>Market</SectionHeading>
    <p>No market data</p>
  </div>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(MarketContainer);