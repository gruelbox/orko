import React from "react"
import Tab from "./primitives/Tab"
import Panel from "./primitives/Panel"
import Para from "./primitives/Para"
import Section from "../components/primitives/Section"
import AlertContainer from "../containers/AlertContainer"
import LimitOrderContainer from "../containers/LimitOrderContainer"

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
          selected={this.state.selected === "limit"}
          onClick={() => this.setState({ selected: "limit" })}
        >
          Limit
        </Tab>
        <Tab
          selected={this.state.selected === "market"}
          onClick={() => this.setState({ selected: "market" })}
        >
          Market
        </Tab>
        <Tab
          selected={this.state.selected === "hardstop"}
          onClick={() => this.setState({ selected: "hardstop" })}
        >
          Hard Stop
        </Tab>
        <Tab
          selected={this.state.selected === "softstop"}
          onClick={() => this.setState({ selected: "softstop" })}
        >
          Soft Stop
        </Tab>
        <Tab
          selected={this.state.selected === "oco"}
          onClick={() => this.setState({ selected: "oco" })}
        >
          Stop/Take Profit
        </Tab>
        <Tab
          selected={this.state.selected === "alert"}
          onClick={() => this.setState({ selected: "alert" })}
        >
          Alert
        </Tab>
        <Tab
          selected={this.state.selected === "complex"}
          onClick={() => this.setState({ selected: "complex" })}
        >
          Complex
        </Tab>
      </span>
    )

    var content = null

    if (!coin) {
      content = <Para>No coin selected</Para>
    } else {
      var panel = null
      if (this.state.selected === "alert") {
        panel = <AlertContainer coin={coin} />
      } else if (this.state.selected === "limit") {
        panel = <LimitOrderContainer coin={coin} />
      }
      content = <Panel mr={3}>{panel}</Panel>
    }

    return (
      <Section id="trading" heading="Trading" buttons={() => buttons}>
        {content}
      </Section>
    )
  }
}
