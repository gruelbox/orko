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
