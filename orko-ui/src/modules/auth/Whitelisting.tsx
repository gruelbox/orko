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
import React, { useState, FormEvent } from "react"
import { Modal, Icon, Form, Button, Message } from "semantic-ui-react"
import { isValidOtp } from "modules/common/util/numberUtils"

export interface WhiteListingProps {
  error?: string
  onApply(response: string): void
}

const WhiteListing: React.FC<WhiteListingProps> = (
  props: WhiteListingProps
) => {
  const [response, setResponse] = useState("")

  const onChangeResponse = (event: FormEvent<HTMLInputElement>) => {
    setResponse(event.currentTarget.value)
  }

  return (
    <Modal open={true} size="tiny" data-orko="whitelistingModal">
      <Modal.Header>
        <Icon name="lock" />
        Unknown or expired origin IP address
      </Modal.Header>
      <Modal.Content>
        <Form
          onSubmit={() => props.onApply(response)}
          error={props.error !== null}
          id="whitelistingForm"
        >
          <Message error header="Error" content={props.error} />
          <Form.Field error={response !== "" && !isValidOtp(response)}>
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
                value={response}
                onChange={onChangeResponse}
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
          disabled={!isValidOtp(response)}
        >
          Authorise
        </Button>
      </Modal.Actions>
    </Modal>
  )
}

export default WhiteListing
