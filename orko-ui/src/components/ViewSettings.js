/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
