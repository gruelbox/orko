import React from "react"
import Tab from "./primitives/Tab"
import Para from "./primitives/Para"
import Section from "../components/primitives/Section"
import LimitOrderContainer from "../containers/LimitOrderContainer"
import StopOrderContainer from "../containers/StopOrderContainer"
import TrailingStopOrderContainer from "../containers/TrailingStopOrderContainer"
import StopTakeProfitContainer from "../containers/StopTakeProfitContainer"
import ScriptContainer from "../containers/ScriptContainer"

export default class TradeSelector extends React.Component {
  constructor(props) {
    super(props)
    this.state = { selected: "limit" }
  }

  render() {
    const coin = this.props.coin

    var buttons = (
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

    var content = null

    if (!coin) {
      content = <Para>No coin selected</Para>
    } else {
      if (this.state.selected === "limit") {
        content = <LimitOrderContainer coin={coin} />
      } else if (this.state.selected === "stop") {
        content = <StopOrderContainer coin={coin} />
      } else if (this.state.selected === "trailing") {
        content = <TrailingStopOrderContainer coin={coin} />
      } else if (this.state.selected === "oco") {
        content = <StopTakeProfitContainer coin={coin} />
      } else if (this.state.selected === "custom") {
        content = <ScriptContainer />
      }
    }

    return (
      <Section
        draggable
        id="trading"
        heading="Trading"
        buttons={() => buttons}
        onHide={this.props.onHide}
      >
        {content}
      </Section>
    )
  }
}
