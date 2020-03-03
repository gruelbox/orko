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
import React from "react"
import { Button, Modal, Icon } from "semantic-ui-react"

export const ErrorPopup: React.FC<{ message: string; onClose(): void }> = ({ message, onClose }) => (
  <Modal
    data-orko="errorModal"
    style={{
      marginTop: "0px !important",
      marginLeft: "auto",
      marginRight: "auto"
    }}
    open={true}
  >
    <Modal.Header>
      <Icon name="warning" />
      Error
    </Modal.Header>
    <Modal.Content>
      <p>{message}</p>
    </Modal.Content>
    <Modal.Actions>
      <Button data-orko="errorSubmit" onClick={() => onClose()}>
        OK
      </Button>
    </Modal.Actions>
  </Modal>
)
