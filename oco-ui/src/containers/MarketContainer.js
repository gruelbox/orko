import React from "react"

import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OrderBook from "./OrderBook"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"

const buttons = () => (
  <span>
    <Tab selected>Top Orders</Tab>
    <Tab>Full Book</Tab>
    <Tab>Market History</Tab>
  </span>
)

const MarketContainer = ({ orderBook, coin }) => (
  <GetPageVisibility>
    {visible => (
      <RenderIf condition={visible}>
        <Section
          alwaysscroll="vertical"
          nopadding
          id="marketData"
          heading="Market"
          buttons={buttons}
        >
          <OrderBook orderBook={orderBook} coin={coin} />
        </Section>
      </RenderIf>
    )}
  </GetPageVisibility>
)

export default MarketContainer