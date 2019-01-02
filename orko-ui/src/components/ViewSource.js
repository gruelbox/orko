import React from "react"
import ScriptEditor from "./ScriptEditor"
import FixedModal from "./primitives/FixedModal"
import { Modal, Icon } from "semantic-ui-react"

const ViewSource = ({ script, onClose }) => (
  <FixedModal
    data-orko="viewScript"
    closeIcon
    size="fullscreen"
    onClose={onClose}
    style={{ height: "100%" }}
  >
    <Modal.Header>
      <Icon name="code" />
      View script
    </Modal.Header>
    <Modal.Content style={{ height: "75vh" }}>
      <ScriptEditor state={script} />
    </Modal.Content>
  </FixedModal>
)

export default ViewSource
