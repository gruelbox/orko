import React, { Component } from 'react';
import { Segment, Header, Icon, Form, Input, Button, Loader } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import { Subscribe } from 'unstated';
import AuthContainer from './context/AuthContainer';

const INACTIVE = 'INACTIVE';
const PROCESSING = 'PROCESSING';
const ERROR = 'ERROR';

export default class Credentials extends Component {

  constructor(props) {
    super(props);
    this.state = {  
      userName: '',
      password: '',
      processingState: INACTIVE,
      endOfProcessingMessage: '',
      errorText: '',
    }
  }

  onChangeUser = (event) => {
    this.setState({ userName: event.target.value });
  }

  onChangePassword = (event) => {
    this.setState({ password: event.target.value });
  }

  onSet = (auth) => {
    this.setState({ processingState: PROCESSING });
    auth.setCredentials(this.state.userName, this.state.password)
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

  onReset = (auth) => {
    this.setState({ username: '', password: '' });
    auth.clearCredentials()
    this.props.onChange();
  }

  render() {
    
    if (this.props.visible === false)
      return null;

    const valid = (auth) => {
      return <Segment>
        <Button type='submit' onClick={() => this.onReset(auth)}>Change credentials</Button>
      </Segment>;
    };

    const notValid = (auth) => {
      return <Segment>
        <Loader active={this.state.processingState === PROCESSING || this.state.processingState === ERROR}>{this.state.endOfProcessingMessage}</Loader>
        <Header as='h2'>
          <Icon name='lock' />
          Credentials
          </Header>
        <Form>
          <Form.Field>
            <label>Username</label>
            <Input type='text' placeholder='Enter user' value={this.state.userName || ''} onChange={this.onChangeUser} />
          </Form.Field>
          <Form.Field>
            <label>Password</label>
            <Input type="password" value={this.state.password || ''} onChange={this.onChangePassword} />
          </Form.Field>
          <Button type='submit' onClick={() => this.onSet(auth)}>Set</Button>
        </Form>
      </Segment>;
    };

    return (
      <Subscribe to={[AuthContainer]}>
        {auth => {
          if (auth.valid && this.state.processingState === INACTIVE) {
            return valid(auth);
          } else {
            return notValid(auth);
          }
        }}
      </Subscribe>
    );
  }
}

Credentials.propTypes = {
  onChange: PropTypes.func
};

Credentials.defaultProps = {
  onChange: () => {}
};