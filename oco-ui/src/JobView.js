import React from 'react';
import { connect } from 'react-redux';

import { Box } from 'rebass';

import Job from './components/Job';

import Section from './components/primitives/Section';
import Para from './components/primitives/Para';
import MidComponentBox from './components/primitives/MidComponentBox';

class JobView extends React.Component {
  render() {

    const job = this.props.jobs.find(j => j.id === this.props.match.params.jobId);

    return (
      <Box flex="1" order={[1, 2]}>
        <MidComponentBox p={2}>
          <Section heading={"Job " + this.props.match.params.jobId}>
            {job && <Job job={job}/>}
            {!job && <Para>Job {this.props.match.params.jobId} not found</Para>}
          </Section>
        </MidComponentBox>
      </Box>
    );
  }
}

function mapStateToProps(state) {
  return {
    jobs: state.job.jobs
  };
}

export default connect(mapStateToProps)(JobView);