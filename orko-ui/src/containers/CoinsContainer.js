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

import { Icon } from "semantic-ui-react"

import * as coinsActions from "../store/coins/actions"
import * as uiActions from "../store/ui/actions"
import { getCoinsForDisplay } from "../selectors/coins"

import Section from "../components/primitives/Section"
import Link from "../components/primitives/Link"
import Coins from "../components/Coins"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"
import { withAuth } from "@orko-ui-auth/Authoriser"

const buttons = () => (
  <Link to="/addCoin" data-orko="addCoin" title="Add a coin">
    <Icon name="add" />
  </Link>
)

const CoinsCointainer = ({ auth, data, dispatch, onHide }) => (
  <GetPageVisibility>
    {visible => (
      <RenderIf condition={visible}>
        <Section id="coinList" heading="Coins" nopadding buttons={buttons}>
          <Coins
            data={data}
            onRemove={coin => dispatch(coinsActions.remove(auth, coin))}
            onClickAlerts={coin => dispatch(uiActions.openAlerts(coin))}
            onClickReferencePrice={coin =>
              dispatch(uiActions.openReferencePrice(coin))
            }
            visible
          />
        </Section>
      </RenderIf>
    )}
  </GetPageVisibility>
)

export default withAuth(
  connect(state => ({
    data: getCoinsForDisplay(state)
  }))(CoinsCointainer)
)
