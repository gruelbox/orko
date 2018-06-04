import React from "react"

import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OpenOrdersContainer from "./OpenOrdersContainer"
import TradeHistoryContainer from "./TradeHistoryContainer"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"

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

    return (
      <GetPageVisibility>
        {visible => (
          <RenderIf condition={visible}>
            <Section nopadding id="orders" heading="Orders" buttons={buttons}>
              {
                this.state.selected === "open"
                  ? <OpenOrdersContainer coin={this.props.coin}/>
                  : <TradeHistoryContainer coin={this.props.coin}/>
              }
            </Section>
          </RenderIf>
        )}
      </GetPageVisibility>
    )
  }
}

export default OrdersContainer