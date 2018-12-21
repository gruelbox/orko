import React, { Component } from "react"
import { Modal, Icon, Form, Button, Message } from "semantic-ui-react"
import FixedModal from "./primitives/FixedModal"

export default class Whitelisting extends Component {
  constructor(props) {
    super(props)
    this.state = {
      response: ""
    }
  }

  onChangeResponse = event => {
    this.setState({ response: event.target.value })
  }

  render() {
    return (
      <FixedModal size="tiny" data-orko="whitelistingModal">
        <Modal.Header>
          <Icon name="lock" />
          Challenge
        </Modal.Header>
        <Modal.Content>
          <Form
            onSubmit={() => this.props.onApply(this.state.response)}
            error={this.props.error !== null}
            id="whitelistingForm"
          >
            <Message error header="Error" content={this.props.error} />
            <Form.Field>
              <label>Response</label>
              <div className="ui input">
                <input
                  data-orko="token"
                  type="text"
                  placeholder="Enter response"
                  value={this.state.response || ""}
                  onChange={this.onChangeResponse}
                />
              </div>
            </Form.Field>
          </Form>
        </Modal.Content>
        <Modal.Actions>
          <Button
            form="whitelistingForm"
            data-orko="whitelistingSubmit"
            type="submit"
          >
            Authorise
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  }
}
