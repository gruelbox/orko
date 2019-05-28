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
import React from 'react'
import Section from '../components/primitives/Section'
import Tab from '../components/primitives/Tab'
import OpenOrdersContainer from './OpenOrdersContainer'
import UserTradeHistoryContainer from './UserTradeHistoryContainer'
import GetPageVisibility from '../components/GetPageVisibility'
import RenderIf from '../components/RenderIf'
import { Checkbox } from 'semantic-ui-react'
import { getValueFromLS, saveValueToLS } from '../util/localStorage'

const ORDER_STORAGE_KEY = 'OrdersContainer.order_show_all'

class OrdersContainer extends React.Component {
  constructor (props) {
    super(props)
    var showAll = getValueFromLS(ORDER_STORAGE_KEY) !== 'false'
    if (showAll === null) showAll = false
    this.state = {
      selected: 'open',
      showAll
    }
  }

  buttons = () => (
    <span>
    <Tab
      selected={this.state.showAll}
      onClick={() => {
        this.setState(
          prev => ({showAll: !prev.showAll}),
          () => saveValueToLS(ORDER_STORAGE_KEY, this.state.showAll)
        )
      }}
      title="Show orders for all coins or selected coins"
      >
        Show All
      </Tab>
      <Tab
        selected={this.state.selected === 'open'}
        onClick={() => this.setState({selected: 'open'})}
        title={this.state.showAll ? 'Show open orders for all coin' : 'Show open orders for the selected coin'}
      >
        Open
      </Tab>
      <Tab
        selected={this.state.selected === 'history'}
        onClick={() => this.setState({selected: 'history'})}
        title={this.state.showAll ? 'Show order history for all coin' : 'Show order history for the selected coin'}
      >
        History
      </Tab>
    </span>
  )

  render () {
    return (
      <GetPageVisibility>
        {visible => (
          <RenderIf condition={visible}>
            <Section
              nopadding
              id="orders"
              heading="Orders"
              buttons={this.buttons}
            >
              {this.state.selected === 'open' ? (
                <OpenOrdersContainer showAll={this.state.showAll}/>
              ) : (
                <UserTradeHistoryContainer showAll={this.state.showAll}/>
              )}
            </Section>
          </RenderIf>
        )}
      </GetPageVisibility>
    )
  }
}

export default OrdersContainer
