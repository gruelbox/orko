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

import styled from "styled-components"
import { space } from "styled-system"

import NotAuthenticated from "../components/NotAuthenticated"

import { getSelectedExchange } from "../selectors/coins"

const Padded = styled.div`
  ${space}
`

const AuthenticatedOnly = ({ exchange, children, padded }) => {
  if (!exchange || exchange.authenticated) {
    return children
  } else {
    return (
      <Padded p={padded ? 2 : 0}>
        <NotAuthenticated exchange={exchange} />
      </Padded>
    )
  }
}

export default connect(state => ({
  exchange: getSelectedExchange(state)
}))(AuthenticatedOnly)
