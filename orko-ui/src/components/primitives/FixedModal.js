import React from "react"
import { Modal } from "semantic-ui-react"

const inlineStyle = {
  modal: {
    marginTop: "0px !important",
    marginLeft: "auto",
    marginRight: "auto"
  }
}

const FixedModal = props => (
  <Modal {...props} dimmer="blurring" open={true} style={inlineStyle.modal} />
)

export default FixedModal
