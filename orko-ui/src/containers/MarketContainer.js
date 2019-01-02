import React from "react"
import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OrderBookContainer from "./OrderBookContainer"
import MarketTradesContainer from "./MarketTradesContainer"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"
import { getValueFromLS, saveValueToLS } from "../util/localStorage"

const LOCAL_STORAGE_KEY = "MarketContainer.animate"

export default class MarketContainer extends React.Component {
  constructor(props) {
    super(props)
    var animateSetting = getValueFromLS(LOCAL_STORAGE_KEY) !== "false"
    if (animateSetting === null) animateSetting = true
    this.state = {
      animate: props.allowAnimate && animateSetting,
      selected: "book"
    }
  }

  buttons = () => (
    <span>
      <Tab
        visible={this.props.allowAnimate}
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
