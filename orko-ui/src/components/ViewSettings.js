import React, { Component } from "react"

import FixedModal from "./primitives/FixedModal"
import { Icon, Modal, Button, Form } from "semantic-ui-react"

export default class ViewSettings extends Component {
  render() {
    return (
      <FixedModal closeIcon onClose={this.props.onClose}>
        <Modal.Header id="viewSettings">
          <Icon name="eye" />
          View settings
        </Modal.Header>
        <Modal.Content>
          <Form>
            {this.props.panels.map(panel => (
              <Form.Checkbox
                key={panel.key}
                label={panel.title}
                type="checkbox"
                checked={panel.visible}
                onChange={() => this.props.onTogglePanelVisible(panel.key)}
              />
            ))}
          </Form>
        </Modal.Content>
        <Modal.Actions>
          <Button onClick={this.props.onReset}>Reset</Button>
          <Button onClick={this.props.onClose} primary>
            OK
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  }
}
