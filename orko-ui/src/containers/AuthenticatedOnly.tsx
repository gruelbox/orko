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
import React, { useContext, ReactElement } from "react"
import styled from "styled-components"
import { space } from "styled-system"
import NotAuthenticated from "../components/NotAuthenticated"
import { MarketContext } from "modules/market"
import { FrameworkContext } from "FrameworkContainer"

const Padded = styled.div`
  ${space}
`

interface AuthenticatedOnlyProps {
  padded?: boolean
  children: ReactElement
}

const AuthenticatedOnly: React.FC<AuthenticatedOnlyProps> = ({ children, padded }) => {
  const exchange = useContext(MarketContext).data.selectedExchange
  const { paperTrading, enablePaperTrading } = useContext(FrameworkContext)
  if (paperTrading || !exchange || exchange.authenticated) {
    return children
  } else {
    return (
      <Padded p={padded ? 2 : 0}>
        <NotAuthenticated exchange={exchange} onEnablePaperTrading={enablePaperTrading} />
      </Padded>
    )
  }
}

AuthenticatedOnly.defaultProps = { padded: false, children: null }

export default AuthenticatedOnly
