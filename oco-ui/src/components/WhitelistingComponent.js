import React, { Component } from 'react';
import { Modal, Icon, Form, Input, Button, Message } from 'semantic-ui-react'
import PropTypes from 'prop-types';

export default class WhitelistingComponent extends Component {

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

    const inlineStyle = {
      modal : {
        marginTop: '0px !important',
        marginLeft: 'auto',
        marginRight: 'auto'
      }
    };

    return (
      <Modal open={true} style={inlineStyle.modal}>   
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
      </Modal>
    );
  }
}

WhitelistingComponent.propTypes = {
  onApply: PropTypes.func,
  error: PropTypes.string
};

WhitelistingComponent.defaultProps = {
  onApply: () => {}
};