import React, { Component } from 'react';
import { Modal, Icon, Form, Input, Button, Message } from 'semantic-ui-react'
import PropTypes from 'prop-types';
import FixedModal from './primitives/FixedModal';

export default class Credentials extends Component {

  constructor(props) {
    super(props);
    this.state = {  
      userName: '',
      password: ''
    }
  }

  onChangeUser = (event) => {
    this.setState({ userName: event.target.value });
  }

  onChangePassword = (event) => {
    this.setState({ password: event.target.value });
  }

  render() {
    return (
      <FixedModal>   
        <Modal.Header><Icon name='lock' />Log in</Modal.Header>
        <Modal.Content>
          <Form error={!!this.props.error}>
            <Message error header='Error' content={this.props.error} />
            <Form.Field>
              <Form.Field>
                <label>Username</label>
                <Input type='text' placeholder='Enter user' value={this.state.userName || ''} onChange={this.onChangeUser} />
              </Form.Field>
              <Form.Field>
                <label>Password</label>
                <Input type="password" value={this.state.password || ''} onChange={this.onChangePassword} />
              </Form.Field>
            </Form.Field>
            <Button type='submit' onClick={() => this.props.onApply(this.state.userName, this.state.password)}>Authorise</Button>
          </Form>
        </Modal.Content>
      </FixedModal>
    );
  }
}

Credentials.propTypes = {
  onApply: PropTypes.func,
  error: PropTypes.string
};

Credentials.defaultProps = {
  onApply: () => {}
};