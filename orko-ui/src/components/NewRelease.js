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
import { Icon, Modal, Button, Segment } from "semantic-ui-react"
import ReactMarkdown from "react-markdown"
import styled from "styled-components"

const Header = styled.h3`
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding-bottom: 4px;
`

const Release = ({ name, body }) => (
  <Segment data-orko={"release/" + name}>
    <Header>{name}</Header>
    <ReactMarkdown source={body} />
  </Segment>
)

export default ({ enabled, releases, onClose, onIgnore }) =>
  enabled && releases && releases.length > 0 ? (
    <FixedModal data-orko="newReleases" closeIcon onClose={onClose}>
      <Modal.Header id="news">
        <Icon name="bell" />
        New version(s) released
      </Modal.Header>
      <Modal.Content>
        {releases.map(r => (
          <Release key={r.name} name={r.name} body={r.body} />
        ))}
      </Modal.Content>
      <Modal.Actions>
        <Button data-orko="later" onClick={onClose}>
          Remind me later
        </Button>
        <Button data-orko="ignore" secondary onClick={onIgnore}>
          Ignore this version
        </Button>
      </Modal.Actions>
    </FixedModal>
  ) : null
