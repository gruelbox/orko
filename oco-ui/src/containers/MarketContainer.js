import React from "react"

import Section from "../components/primitives/Section"
import Tab from "../components/primitives/Tab"
import OrderBook from "./OrderBook"

const buttons = () => (
  <span>
    <Tab selected>Top Orders</Tab>
    <Tab>Full Book</Tab>
    <Tab>Market History</Tab>
  </span>
)

const MarketContainer = ({ orderBook, coin }) => {
  const content = <OrderBook orderBook={orderBook}coin={coin}/>
  return (
    <Section
      alwaysscroll="vertical"
      nopadding
      id="marketData"
      heading="Market"
      buttons={buttons}
    >
      {content}
    </Section>
  )
}

export default MarketContainer