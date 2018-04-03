import React from 'react';
import { connect } from 'react-redux';
import * as jobActions from '../store/job/actions';

import Section from '../components/primitives/Section';
import Para from '../components/primitives/Para';
import JobShort from '../components/JobShort';

const TICK_TIME = 5000;

class JobsContainer extends React.Component {

  tick = () => {
    this.props.dispatch(jobActions.fetchJobs());
  }

  componentDidMount() {
    this.tick();
    this.interval = setInterval(this.tick, TICK_TIME);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  render() {

    var jobs;
    if (this.props.jobs.length === 0) {
      jobs = <Para>No active jobs</Para>;
    } else {
      jobs = this.props.jobs.map(job => <JobShort job={job} />);
    }

    return (
      <Section heading="Running Jobs">
        {jobs}
      </Section>
    );
  }
};

function mapStateToProps(state) {
  return {
    jobs: state.job.jobs
  };
}

export default connect(mapStateToProps)(JobsContainer);