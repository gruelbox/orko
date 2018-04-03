import React from 'react';
import { connect } from 'react-redux';

import { Box } from 'rebass';

import Job from './components/Job';

import Section from './components/primitives/Section';
import Para from './components/primitives/Para';
import MidComponentBox from './components/primitives/MidComponentBox';

import jobService from './services/job';
import * as authActions from './store/auth/actions';

class JobView extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      job: null,
      error: null
    };
  }

  async componentDidMount() {
    const response = await jobService.fetchJob(this.props.match.params.jobId, this.props.auth.token);
    if (!response.ok) {
      const authAction = authActions.handleHttpResponse(response);
      if (authAction !== null) {
        this.props.dispatch(authAction);
      } else {
        this.setState({ job: null, error: response.statusText });
      }
    } else {
      this.setState({ job: response.json(), error: null });
    }
  }

  render() {

    const jobId = this.props.match.params.jobId;

    return (
      <Box flex="1" order={[1, 2]}>
        <MidComponentBox p={2}>
          <Section heading={"Job " + jobId}>
            {this.state.job && <Job job={this.state.job}/>}
            {this.state.error && <Para>{this.state.error}</Para>}
          </Section>
        </MidComponentBox>
      </Box>
    );
  }
}

function mapStateToProps(state) {
  return {
    auth: state.auth
  };
}

export default connect(mapStateToProps)(JobView);