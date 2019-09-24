import React from "react"
import { Button, Modal, Icon } from "semantic-ui-react"

export const ErrorPopup: React.FC<{ message: string; onClose(): void }> = ({ message, onClose }) => (
  <Modal data-orko="errorModal">
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
