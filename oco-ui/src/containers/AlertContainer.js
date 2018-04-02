import React from 'react';
import { connect } from 'react-redux';

import Immutable from 'seamless-immutable';
import { Flex, Box } from 'rebass';

import Alert from '../components/Alert';
import Job from '../components/Job';

import * as authActions from '../store/auth/actions';
import * as focusActions from '../store/focus/actions';
import jobService from '../services/job';

class AlertContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      job: Immutable({
        highPrice: '',
        lowPrice: '',
        message: 'Alert'
      }),
      error: null,
      success: false
    }
  }

  onChange = job => {
    this.setState({
      job: job,
      error: null,
      success: false
    })
  };

  onFocus = focusedProperty => {
    this.props.dispatch(
      focusActions.setUpdateAction(value => {
        console.log("Set focus to" + focusedProperty);
        this.setState(prev => ({
          job: prev.job.merge({
            [focusedProperty]: value
          }) 
        }))
      })
    )
  }

  createJob = () => {

    const isValidNumber = (val) => !isNaN(val) && val !== '' && val > 0;
    const highPriceValid = this.state.job.highPrice && isValidNumber(this.state.job.highPrice);
    const lowPriceValid = this.state.job.lowPrice && isValidNumber(this.state.job.lowPrice);

    const tickTrigger = {
      exchange: this.props.coin.exchange,
      counter: this.props.coin.counter,
      base: this.props.coin.base
    };

    return {
      jobType: "OneCancelsOther",
      tickTrigger: tickTrigger,
      low: lowPriceValid ? {
          thresholdAsString: String(this.state.job.lowPrice),
          job: {
            jobType: "Alert",
            message: "Price of " + this.props.coin.name + " dropped below [" + this.state.job.lowPrice + "]: " + this.state.job.message
          }
      } : null,
      high: highPriceValid ? {
        thresholdAsString: String(this.state.job.highPrice),
        job: {
          jobType: "Alert",
          message: "Price of " + this.props.coin.name + " rose above [" + this.state.job.highPrice + "]: " + this.state.job.message
        }
      } : null
    }
  };

  onSubmit = async() => {

    const job = this.createJob();

    this.setState(
      { error: null, success: false },
      async() => {
        const response = await jobService.submitJob(job, this.props.auth.token);
        if (!response.ok) {
          const authAction = authActions.handleHttpResponse(response);
          if (authAction !== null) {
            this.props.dispatch(authAction);
          } else {
            this.setState({ error: response.statusText });
          }
        } else {
          this.setState({ success: true });
        }
      }
    );
  }

  render() {

    const isValidNumber = (val) => !isNaN(val) && val !== '' && val > 0;
    const highPriceValid = this.state.job.highPrice && isValidNumber(this.state.job.highPrice);
    const lowPriceValid = this.state.job.lowPrice && isValidNumber(this.state.job.lowPrice);
    const messageValid = this.state.job.message !== "";

    return (
      <Flex flexWrap='wrap'>
        <Box width={1/3}>
          <Alert
            job={this.state.job}
            onChange={this.onChange}
            onFocus={this.onFocus}
            onSubmit={this.onSubmit}
            highPriceValid={highPriceValid}
            lowPriceValid={lowPriceValid}
            messageValid={messageValid}
            success={this.state.success}
            error={this.state.error}
          />
        </Box>
        <Box width={2/3}>
          <Job job={this.createJob()} />
        </Box>
      </Flex>
    );
  }
}

function mapStateToProps(state) {
  return {
    auth: state.auth
  };
}

export default connect(mapStateToProps)(AlertContainer);