import React from 'react';
import { connect } from 'react-redux';

import { Tabs } from 'rebass';

import SectionHeading from '../components/primitives/SectionHeading';
import Para from '../components/primitives/Para';
import Tab from '../components/primitives/Tab';

const MarketContainer = props => (
  <div>
    <SectionHeading>Market</SectionHeading>
    <Tabs mb={3}>
      <Tab selected>
        Tab book
      </Tab>
      <Tab>
        Trade history
      </Tab>
    </Tabs>
    <Para>No market data</Para>
  </div>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(MarketContainer);