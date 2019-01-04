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
