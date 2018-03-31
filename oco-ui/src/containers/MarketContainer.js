import React from 'react';
import { connect } from 'react-redux';

import { Tabs } from 'rebass';

import SectionHeading from '../components/primitives/SectionHeading';
import ForePara from '../components/primitives/ForePara';
import ForeTab from '../components/primitives/ForeTab';

const MarketContainer = props => (
  <div>
    <SectionHeading>Market</SectionHeading>
    <Tabs>
      <ForeTab selected>
        Order book
      </ForeTab>
      <ForeTab>
        Trade history
      </ForeTab>
    </Tabs>
    <ForePara>No market data</ForePara>
  </div>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(MarketContainer);