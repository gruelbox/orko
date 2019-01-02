/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
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
                  placeholder="Enter response..."
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
