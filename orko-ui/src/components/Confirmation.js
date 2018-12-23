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
