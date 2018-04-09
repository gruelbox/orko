import React from 'react';
import { connect } from 'react-redux';
import * as jobActions from '../store/job/actions';

import Section from '../components/primitives/Section';
import Para from '../components/primitives/Para';
import Loading from '../components/primitives/Loading';
import JobShort from '../components/JobShort';

const TICK_TIME = 5000;

class JobsContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {loading: true};
  }

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

  componentWillReceiveProps(nextProps) {
    if (nextProps.jobs)
      this.setState({ loading: false });
  }

  onRemove = (job) => {
    this.props.dispatch(jobActions.deleteJob(job));
  };

  render() {
    const onRemove = this.onRemove;

    var jobs;
    if (this.state.loading) {
      jobs = <Loading/>;
    } else if (this.props.jobs.length === 0) {
      jobs = <Para>No active jobs</Para>;
    } else {
      jobs = this.props.jobs.map(job =>
        <JobShort key={job.id} job={job} onRemove={() => onRemove(job)} />
      );
    }

    return (
      <Section id="jobs" heading="Running Jobs">
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