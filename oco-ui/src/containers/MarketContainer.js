import React from "react"

import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OrderBook from "./OrderBook"
import MarketTradesContainer from "./MarketTradesContainer"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"

import { getValueFromLS, saveValueToLS } from "../util/localStorage"

const LOCAL_STORAGE_KEY = "MarketContainer.animate"

export default class MarketContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      animate: getValueFromLS(LOCAL_STORAGE_KEY),
      selected: "book"
    }
    if (this.state.animate === null) this.state.animate = true
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
      >
        Animate
      </Tab>
      <Tab
        selected={this.state.selected === "book"}
        onClick={() => this.setState({ selected: "book" })}
      >
        Top Orders
      </Tab>
      <Tab
        selected={this.state.selected === "history"}
        onClick={() => this.setState({ selected: "history" })}
      >
        Market History
      </Tab>
    </span>
  )

  render() {
    const { coin } = this.props
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
                <OrderBook coin={coin} animate={this.state.animate} />
              ) : (
                <MarketTradesContainer coin={this.props.coin} />
              )}
            </Section>
          </RenderIf>
        )}
      </GetPageVisibility>
    )
  }
}
