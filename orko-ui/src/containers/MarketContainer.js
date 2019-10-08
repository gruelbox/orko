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
import OrderBookContainer from "./OrderBookContainer"
import MarketTradesContainer from "./MarketTradesContainer"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"
import {
  getValueFromLS,
  saveValueToLS
} from "modules/common/util/localStorage"

const LOCAL_STORAGE_KEY = "MarketContainer.animate"

export default class MarketContainer extends React.Component {
  constructor(props) {
    super(props)
    var animate = getValueFromLS(LOCAL_STORAGE_KEY) !== "false"
    if (animate === null) animate = true
    this.state = {
      animate,
      selected: "book"
    }
  }

  buttons = () => (
    <span>
      <Tab
        selected={this.state.animate}
        onClick={() => {
          this.setState(
            prev => ({ animate: !prev.animate }),
            () => saveValueToLS(LOCAL_STORAGE_KEY, this.state.animate)
          )
        }}
        title="Enables and disables animation, to save CPU"
      >
        Animate
      </Tab>
      <Tab
        selected={this.state.selected === "book"}
        onClick={() => this.setState({ selected: "book" })}
        title="The top of the order book, in detail"
      >
        Top Orders
      </Tab>
      <Tab
        selected={this.state.selected === "history"}
        onClick={() => this.setState({ selected: "history" })}
        title="Live trades on the exchange"
      >
        Market History
      </Tab>
    </span>
  )

  render() {
    return (
      <GetPageVisibility>
        {visible => (
          <RenderIf condition={visible}>
            <Section
              alwaysscroll="vertical"
              nopadding
              id="marketData"
              heading="Market"
              buttons={this.buttons}
            >
              {this.state.selected === "book" ? (
                <OrderBookContainer animate={this.state.animate} />
              ) : (
                <MarketTradesContainer />
              )}
            </Section>
          </RenderIf>
        )}
      </GetPageVisibility>
    )
  }
}
