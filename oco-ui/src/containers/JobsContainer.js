import React from 'react';
import { connect } from 'react-redux';

import SectionHeading from '../components/primitives/SectionHeading';
import Para from '../components/primitives/Para';

const JobsContainer = props => (
  <div>
    <SectionHeading>Running Jobs</SectionHeading>
    <Para>No data</Para>
  </div>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(JobsContainer);