import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Segment, Header, Icon, Form, Input, Button } from 'semantic-ui-react'
import { setCredentials, clearCredentials } from './redux/auth';
import PropTypes from 'prop-types';

class Credentials extends Component {

  constructor(props) {
    super(props);
    this.state = {
      username: this.props.auth.userName,
      password: this.props.auth.password
    }
  }

  onChangeUser = (event) => {
    this.setState({ userName: event.target.value });
  }

  onChangePassword = (event) => {
    this.setState({ password: event.target.value });
  }

  onSet = () => {
    this.props.setCredentials(this.state.userName, this.state.password)
      .then(() => this.props.onChange());
  }

  onReset = () => {
    this.setState({ username: '', password: '' });
    this.props.clearCredentials()
    this.props.onChange();
  }

  render() {
    if (this.props.visible === false)
      return false;
    if (this.props.auth.valid) {
      return (
        <Segment>
          <Button type='submit' onClick={this.onReset}>Change credentials</Button>
        </Segment>
      )
    } else {
      return (
        <Segment>
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
            <Button type='submit' onClick={this.onSet}>Set</Button>
          </Form>
        </Segment>
      )
    }
  }
}

Credentials.propTypes = {
  visible: PropTypes.bool,
  onChange: PropTypes.func
};

Credentials.defaultProps = {
  visible: true,
  onChange: () => {}
};

const mapStateToProps = state => ({
  auth: state.auth
});

const mapDispatchToProps = {
  setCredentials: setCredentials,
  clearCredentials: clearCredentials,
};

export default connect(mapStateToProps, mapDispatchToProps)(Credentials);