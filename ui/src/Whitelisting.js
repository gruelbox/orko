import React, { Component } from 'react';
import { Segment, Header, Icon, Form, Input, Button, Loader } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import { Subscribe  } from 'unstated';
import { AuthContainer  } from './context/AuthContainer';

const INACTIVE = 'INACTIVE';
const PROCESSING = 'PROCESSING';
const ERROR = 'ERROR';

export default class Whitelisting extends Component {

  constructor(props) {
    super(props);
    this.state = {  
      token: '',
      processingState: INACTIVE,
      endOfProcessingMessage: '',
      errorText: '',
    }
  }

  onChangeValue = (event) => this.setState({ token: event.target.value });

  onSubmit = (auth) => {
    this.setState({ processingState: PROCESSING });
    auth.whitelist(this.state.token)
      .then(() => {
        this.setState({ endOfProcessingMessage: "Success" });
        this.props.onChange();
        setTimeout(
          () => this.setState({
            processingState: INACTIVE,
            errorText: '',
            endOfProcessingMessage: ''
          }),
          1000
        );
      })
      .catch(error => {
        this.setState({ endOfProcessingMessage: error.message });
        this.props.onChange();
        setTimeout(
          () => this.setState({
            processingState: INACTIVE,
            errorText: '',
            endOfProcessingMessage: ''
          }),
          2000
        );
      });
  };

  closeDialog = () => this.setState({ processingState: INACTIVE })

  render() {
    return (
      <Segment>
        <Loader active={this.state.processingState === PROCESSING || this.state.processingState === ERROR}>{this.state.endOfProcessingMessage}</Loader>
        <Header as='h2'>
          <Icon name='lock' />
          Whitelisting
          </Header>
        <Form>
          <Form.Field>
            <label>Token</label>
            <Input type='text' placeholder='Enter token' value={this.state.token || ''} onChange={this.onChangeValue} />
          </Form.Field>
          <Subscribe to={[AuthContainer]}>
            {auth =>
              <Button type='submit' onClick={event => this.onSubmit(auth)}>Authorise</Button>
            }
          </Subscribe>
        </Form>
      </Segment>
    );
  }
}

Whitelisting.propTypes = {
  onChange: PropTypes.func
};

Whitelisting.defaultProps = {
  onChange: () => {}
};