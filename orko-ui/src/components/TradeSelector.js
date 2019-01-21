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
import Tab from "./primitives/Tab"
import Section from "../components/primitives/Section"
import LimitOrderContainer from "../containers/LimitOrderContainer"
import StopOrderContainer from "../containers/StopOrderContainer"
import TrailingStopOrderContainer from "../containers/TrailingStopOrderContainer"
import StopTakeProfitContainer from "../containers/StopTakeProfitContainer"
import ScriptContainer from "../containers/ScriptContainer"
import WithCoin from "../containers/WithCoin"
import AuthenticatedOnly from "../containers/AuthenticatedOnly"

export default class TradeSelector extends React.Component {
  constructor(props) {
    super(props)
    this.state = { selected: "limit" }
  }

  Buttons = () => (
    <>
      <Tab
        data-orko="limit"
        selected={this.state.selected === "limit"}
        onClick={() => this.setState({ selected: "limit" })}
        title="Simple limit orders"
      >
        Limit
      </Tab>
      <Tab
        data-orko="stop"
        selected={this.state.selected === "stop"}
        onClick={() => this.setState({ selected: "stop" })}
        title="Simple stop orders, processed either on the exchange or by Orko"
      >
        Stop
      </Tab>
      <Tab
        data-orko="trailing"
        selected={this.state.selected === "trailing"}
        onClick={() => this.setState({ selected: "trailing" })}
        title="Trailing stops, handled by Orko"
      >
        Trailing stop
      </Tab>
      <Tab
        data-orko="stopTakeProfit"
        selected={this.state.selected === "oco"}
        onClick={() => this.setState({ selected: "oco" })}
        title="Complex one-cancels-other orders"
      >
        OCO
      </Tab>
      <Tab
        selected={this.state.selected === "custom"}
        onClick={() => this.setState({ selected: "custom" })}
        title="Custom scripted orders"
      >
        Custom script
      </Tab>
    </>
  )

  render() {
    return (
      <Section
        id="trading"
        heading={
          this.props.exchange && this.props.exchange.authenticated
            ? "Trading"
            : "Paper Trading"
        }
        buttons={this.Buttons}
      >
        <AuthenticatedOnly>
          <WithCoin>
            {coin => {
              if (this.state.selected === "limit") {
                return <LimitOrderContainer key={coin.key} coin={coin} />
              } else if (this.state.selected === "stop") {
                return <StopOrderContainer key={coin.key} coin={coin} />
              } else if (this.state.selected === "trailing") {
                return <TrailingStopOrderContainer key={coin.key} coin={coin} />
              } else if (this.state.selected === "oco") {
                return <StopTakeProfitContainer key={coin.key} coin={coin} />
              } else if (this.state.selected === "custom") {
                return <ScriptContainer />
              } else {
                return null
              }
            }}
          </WithCoin>
        </AuthenticatedOnly>
      </Section>
    )
  }
}
