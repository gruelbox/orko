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
import { Modal, Icon, Button, Form } from "semantic-ui-react"

const ScriptParameterEditor = ({
  parameter,
  existing,
  onChange,
  onDelete,
  onUpdate,
  onClose,
  valid
}) => (
  <FixedModal data-orko="editParameter">
    <Modal.Header>
      <Icon name="question" />
      Edit Parameter
    </Modal.Header>
    <Modal.Content>
      <Form>
        <Form.Input
          required
          id="name"
          label="Name"
          disabled={existing}
          placeholder="Enter name..."
          value={parameter.name}
          onChange={e => onChange({ ...parameter, name: e.target.value })}
        />
        <Form.Input
          required
          id="description"
          label="Description"
          placeholder="Enter description..."
          value={parameter.description}
          onChange={e =>
            onChange({ ...parameter, description: e.target.value })
          }
        />
        <Form.Input
          id="default"
          label="Default value"
          placeholder="(Optional)"
          value={parameter.default}
          onChange={e => onChange({ ...parameter, default: e.target.value })}
        />
        <Form.Checkbox
          id="mandatory"
          label="Mandatory"
          checked={parameter.mandatory}
          onChange={e =>
            onChange({ ...parameter, mandatory: e.target.checked })
          }
        />
      </Form>
    </Modal.Content>
    <Modal.Actions>
      <Button
        data-orko="delete"
        negative
        floated="left"
        disabled={!existing}
        onClick={onDelete}
      >
        Delete
      </Button>
      <Button data-orko="update" disabled={!valid} onClick={onUpdate}>
        Update
      </Button>
      <Button secondary data-orko="cancel" onClick={onClose}>
        Cancel
      </Button>
    </Modal.Actions>
  </FixedModal>
)

export default ScriptParameterEditor
