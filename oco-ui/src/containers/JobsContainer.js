import React from 'react';
import { connect } from 'react-redux';

import SectionHeading from '../components/primitives/SectionHeading';
import ForePara from '../components/primitives/ForePara';

const JobsContainer = props => (
  <div>
    <SectionHeading>Running Jobs</SectionHeading>
    <ForePara>No data</ForePara>
  </div>
);

function mapStateToProps(state) {
  return {
  };
}

export default connect(mapStateToProps)(JobsContainer);