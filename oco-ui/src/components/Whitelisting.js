import React, { Component } from 'react';
import { Modal, Icon, Form, Input, Button, Message } from 'semantic-ui-react'
import FixedModal from './primitives/FixedModal';
import PropTypes from 'prop-types';

export default class Whitelisting extends Component {

  constructor(props) {
    super(props);
    this.state = {  
      response: ''
    }
  }

  onChangeResponse = (event) => {
    this.setState({ response: event.target.value });
  }

  render() {
    return (
      <FixedModal>   
        <Modal.Header><Icon name='lock' />Challenge</Modal.Header>
        <Modal.Content>
          <Form error={this.props.error !== null}>
            <Message error header='Error' content={this.props.error} />
            <Form.Field>
              <label>Response</label>
              <Input type='text' placeholder='Enter response' value={this.state.response || ''} onChange={this.onChangeResponse} />
            </Form.Field>
            <Button type='submit' onClick={() => this.props.onApply(this.state.response)}>Authorise</Button>
          </Form>
        </Modal.Content>
      </FixedModal>
    );
  }
}

Whitelisting.propTypes = {
  onApply: PropTypes.func,
  error: PropTypes.string
};

Whitelisting.defaultProps = {
  onApply: () => {}
};