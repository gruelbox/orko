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
import { connect } from "react-redux"
import { Button, Modal, Icon } from "semantic-ui-react"
import FixedModal from "../components/primitives/FixedModal"

import * as errorActions from "../store/error/actions"

const ErrorContainer = props => {
  if (props.errorForeground !== null) {
    return (
      <FixedModal data-orko="errorModal">
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
