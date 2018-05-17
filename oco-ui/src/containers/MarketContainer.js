import React from "react"

import Section from "../components/primitives/Section"
import Para from "../components/primitives/Para"
import Tab from "../components/primitives/Tab"

const MarketContainer = props => (
  <Section id="marketData" heading="Market" buttons={() => (
    <span>
      <Tab selected>Order Book</Tab>
      <Tab>History</Tab>
    </span>
  )}>
    <Para>No market data</Para>
  </Section>
)

export default MarketContainer