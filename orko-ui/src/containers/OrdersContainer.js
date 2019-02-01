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
import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OpenOrdersContainer from "./OpenOrdersContainer"
import UserTradeHistoryContainer from "./UserTradeHistoryContainer"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"

class OrdersContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = { selected: "open" }
  }

  buttons = () => (
    <span>
      <Tab
        selected={this.state.selected === "open"}
        onClick={() => this.setState({ selected: "open" })}
        title="Show open orders for the selected coin"
      >
        Open
      </Tab>
      <Tab
        selected={this.state.selected === "history"}
        onClick={() => this.setState({ selected: "history" })}
        title="Show order history for the selected coin"
      >
        History
      </Tab>
    </span>
  )

  render() {
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
              {this.state.selected === "open" ? (
                <OpenOrdersContainer />
              ) : (
                <UserTradeHistoryContainer />
              )}
            </Section>
          </RenderIf>
        )}
      </GetPageVisibility>
    )
  }
}

export default OrdersContainer
