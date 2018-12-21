import React from "react"
import { connect } from "react-redux"
import { Button, Modal, Icon } from "semantic-ui-react"
import FixedModal from "../components/primitives/FixedModal"

import * as errorActions from "../store/error/actions"

const ErrorContainer = props => {
  if (props.errorForeground !== null) {
    return (
      <FixedModal defaultOpen data-orko="errorModal">
        <Modal.Header>
          <Icon name="warning" />
          Error
        </Modal.Header>
        <Modal.Content>
          <p>{props.errorForeground}</p>
        </Modal.Content>
        <Modal.Actions>
          <Button
            data-orko="errorSubmit"
            onClick={() => props.dispatch(errorActions.clearForeground())}
          >
            OK
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  } else {
    return null
  }
}

function mapStateToProps(state) {
  return {
    errorForeground: state.error.errorForeground
  }
}

export default connect(mapStateToProps)(ErrorContainer)
