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
import React, { useContext } from "react"
import { connect } from "react-redux"

import styled from "styled-components"
import { space } from "styled-system"

import NotAuthenticated from "../components/NotAuthenticated"

import * as uiActions from "../store/ui/actions"
import { MarketContext } from "@orko-ui-market/index"

const Padded = styled.div`
  ${space}
`

const Inner = ({ exchange, children, padded, dispatch, paperTrading }) => {
  if (paperTrading || !exchange || exchange.authenticated) {
    return children
  } else {
    return (
      <Padded p={padded ? 2 : 0}>
        <NotAuthenticated
          exchange={exchange}
          onEnablePaperTrading={() => dispatch(uiActions.acceptPaperTrading())}
        />
      </Padded>
    )
  }
}

const AddExchange = props => {
  const marketApi = useContext(MarketContext)
  return <Inner {...props} exchange={marketApi.data.selectedExchange} />
}

const Connected = connect(state => ({
  paperTrading: state.ui.paperTrading
}))(AddExchange)

export default Connected
