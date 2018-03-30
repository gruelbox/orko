import React, { Component } from 'react';
import { Modal, Icon, Form, Input, Button, Message } from 'semantic-ui-react'
import PropTypes from 'prop-types';

export default class Whitelisting extends Component {

  constructor(props) {
    super(props);
    this.state = {  
      token: ''
    }
  }

  onChangeToken = (event) => {
    this.setState({ token: event.target.value });
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
        <Modal.Header><Icon name='lock' />Token Required</Modal.Header>
        <Modal.Content>
          <Form error={this.props.error !== undefined}>
            <Message error header='Error' content={this.props.error} />
            <Form.Field>
              <label>Token</label>
              <Input type='text' placeholder='Enter token' value={this.state.token || ''} onChange={this.onChangeToken} />
            </Form.Field>
            <Button type='submit' onClick={() => this.props.onApply(this.state.token)}>Authorise</Button>
          </Form>
        </Modal.Content>
      </Modal>
    );
  }
}

Whitelisting.propTypes = {
  onApply: PropTypes.func,
  open: PropTypes.bool,
  error: PropTypes.string
};

Whitelisting.defaultProps = {
  onApply: () => {}
};