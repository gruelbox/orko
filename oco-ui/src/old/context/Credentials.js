import React, { Component } from 'react';
import { Modal, Icon, Form, Input, Button, Message } from 'semantic-ui-react'
import PropTypes from 'prop-types';

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

    const inlineStyle = {
      modal : {
        marginTop: '0px !important',
        marginLeft: 'auto',
        marginRight: 'auto'
      }
    };

    return (
      <Modal open={this.props.open} style={inlineStyle.modal}>   
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
      </Modal>
    );
  }
}

Credentials.propTypes = {
  onApply: PropTypes.func,
  open: PropTypes.bool,
  error: PropTypes.string
};

Credentials.defaultProps = {
  onApply: () => {}
};