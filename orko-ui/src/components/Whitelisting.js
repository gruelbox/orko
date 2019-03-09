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
import { Modal, Icon, Form, Button, Message } from "semantic-ui-react"
import FixedModal from "./primitives/FixedModal"
import { isValidOtp } from "../util/numberUtils"

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
          Unknown or expired origin IP address
        </Modal.Header>
        <Modal.Content>
          <Form
            onSubmit={() => this.props.onApply(this.state.response)}
            error={this.props.error !== null}
            id="whitelistingForm"
          >
            <Message error header="Error" content={this.props.error} />
            <Form.Field
              error={
                this.state.response !== "" && !isValidOtp(this.state.response)
              }
            >
              <label>
                Enter one-time password{" "}
                <Icon
                  name="question circle"
                  title="To access from this IP address, enter a one-time-password from an authenticator application such as Google Authenticator. This must be configured with the same shared secret as is stored on the server in the config file (auth/ipWhitelisting/secretKey) or the AUTH_TOKEN environment variable."
                />
              </label>
              <div className="ui input">
                <input
                  data-orko="token"
                  type="text"
                  placeholder="6 digits, e.g. 123456"
                  value={this.state.response}
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
            disabled={!isValidOtp(this.state.response)}
          >
            Authorise
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  }
}
