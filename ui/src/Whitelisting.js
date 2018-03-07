import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Segment, Header, Icon, Form, Input, Button, Modal } from 'semantic-ui-react'
import { whitelist } from './redux/auth';
import PropTypes from 'prop-types';

class Whitelisting extends Component {

  constructor(props) {
    super(props);
    this.state = {  
      token: '',
      showDialog: false
    }
  }

  onChangeValue = (event) => this.setState({ token: event.target.value });

  onSubmit = () => this.props.whitelist(this.state.token)
      .then(() => {
        this.props.onChange();
        this.setState({ showDialog: true });
      });

  closeDialog = () => this.setState({ showDialog: false })

  render() {
    return (
      <div>
        <Segment>
          <Header as='h2'>
            <Icon name='lock' />
            Whitelisting
            </Header>
          <Form>
            <Form.Field>
              <label>Token</label>
              <Input type='text' placeholder='Enter token' value={this.state.token || ''} onChange={this.onChangeValue} />
            </Form.Field>
            <Button type='submit' onClick={this.onSubmit}>Authorise</Button>
          </Form>
        </Segment>
        <Modal style={{position: "relative", top: 100}} size="tiny" dimmer="blurring" open={this.state.showDialog && !this.props.auth.whitelisted} onClose={this.closeDialog}>
          <Modal.Content><p>Whitelisting failed</p></Modal.Content>
          <Modal.Actions><Button color='black' onClick={this.closeDialog}>OK</Button></Modal.Actions>
        </Modal>
      </div>
    )
  }
}

Whitelisting.propTypes = {
  onChange: PropTypes.func
};

Whitelisting.defaultProps = {
  onChange: () => {}
};

const mapStateToProps = state => ({
  auth: state.auth
});

const mapDispatchToProps = {
  whitelist: whitelist
};

export default connect(mapStateToProps, mapDispatchToProps)(Whitelisting);