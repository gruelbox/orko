import React from "react"

import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OrderBook from "./OrderBook"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"

import { getValueFromLS, saveValueToLS } from "../util/localStorage"

const LOCAL_STORAGE_KEY = "MarketContainer.animate"

export default class MarketContainer extends React.Component {

  constructor(props) {
    super(props)
    this.state = {
      animate: getValueFromLS(LOCAL_STORAGE_KEY)
    }
    if (this.state.animate === null)
      this.state.animate = true
  }

  render() {
    const { coin } = this.props
    const { animate } = this.state
    return (
      <GetPageVisibility>
        {visible => (
          <RenderIf condition={visible}>
            <Section
              alwaysscroll="vertical"
              nopadding
              id="marketData"
              heading="Market"
              buttons={() => (
                <span>
                  <Tab selected={animate} onClick={() => {
                    this.setState(
                      prev => ({ animate: !prev.animate }),
                      () => saveValueToLS(LOCAL_STORAGE_KEY, this.state.animate)
                    )
                  }}>Animate</Tab>
                  <Tab selected>Top Orders</Tab>
                  <Tab>Full Book</Tab>
                  <Tab>Market History</Tab>
                </span>
              )}
            >
              <OrderBook coin={coin} animate={animate} />
            </Section>
          </RenderIf>
        )}
      </GetPageVisibility>
    )
  }
}