import React from "react"
import Tab from "./primitives/Tab"
import Para from "./primitives/Para"
import Section from "../components/primitives/Section"
import LimitOrderContainer from "../containers/LimitOrderContainer"
import StopTakeProfitContainer from "../containers/StopTakeProfitContainer"

export default class TradeSelector extends React.Component {
  constructor(props) {
    super(props)
    this.state = { selected: "limit" }
  }

  render() {
    const coin = this.props.coin

    var buttons = (
      <span>
        <Tab
          data-orko="limit"
          selected={this.state.selected === "limit"}
          onClick={() => this.setState({ selected: "limit" })}
        >
          Limit
        </Tab>
        <Tab
          data-orko="stopTakeProfit"
          selected={this.state.selected === "oco"}
          onClick={() => this.setState({ selected: "oco" })}
        >
          Stop/Take Profit
        </Tab>
        <Tab
          selected={this.state.selected === "complex"}
          onClick={() => this.setState({ selected: "complex" })}
        >
          More to come...
        </Tab>
      </span>
    )

    var content = null

    if (!coin) {
      content = <Para>No coin selected</Para>
    } else {
      if (this.state.selected === "limit") {
        content = <LimitOrderContainer coin={coin} />
      } else if (this.state.selected === "oco") {
        content = <StopTakeProfitContainer coin={coin} />
      }
    }

    return (
      <Section id="trading" heading="Trading" buttons={() => buttons}>
        {content}
      </Section>
    )
  }
}
