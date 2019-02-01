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
import FixedModal from "./primitives/FixedModal"
import { Modal, Icon, Button } from "semantic-ui-react"

const Confirmation = ({ message, onCancel, onOk }) => (
  <FixedModal data-orko="confirmation" size="small">
    <Modal.Header>
      <Icon name="warning" />
      Confirmation
    </Modal.Header>
    <Modal.Content>{message}</Modal.Content>
    <Modal.Actions>
      <Button data-orko="confirmCancel" onClick={() => onCancel && onCancel()}>
        Cancel
      </Button>
      <Button data-orko="confirmOk" onClick={() => onOk && onOk()}>
        OK
      </Button>
    </Modal.Actions>
  </FixedModal>
)

export default Confirmation
