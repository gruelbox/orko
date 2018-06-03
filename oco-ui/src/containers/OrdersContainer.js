import React from "react"

import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OpenOrdersContainer from "./OpenOrdersContainer"
import TradeHistoryContainer from "./TradeHistoryContainer"

class OrdersContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = { selected: "open" }
  }

  render() {

    const buttons = () => (
      <span>
        <Tab
          selected={this.state.selected === "open"}
          onClick={() => this.setState({ selected: "open" })}
        >
          Open
        </Tab>
        <Tab
          selected={this.state.selected === "history"}
          onClick={() => this.setState({ selected: "history" })}
        >
          History
        </Tab>
      </span>
    )

    var component
    if (this.state.selected === "open") {
      component = <OpenOrdersContainer coin={this.props.coin}/>
    } else {
      component = <TradeHistoryContainer coin={this.props.coin}/>
    }

    return (
      <Section nopadding id="orders" heading="Orders" buttons={buttons}>
        {component}
      </Section>
    )
  }
}

export default OrdersContainer