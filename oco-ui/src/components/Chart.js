import React, { Component } from "react"
import styled from "styled-components"

import Section from "./primitives/Section"
import Para from "./primitives/Para"

const CONTAINER_ID = "tradingview-widget-container"

class ChartContent extends Component {
  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return this.props.coin.key !== nextProps.coin.key
  }

  componentDidMount = () => {
    this.reLoad()
  }

  componentDidUpdate = () => {
    this.reLoad()
  }

  reLoad = () => {
    document.getElementById(CONTAINER_ID).innerHTML = ""
    this.initWidget()
  }

  initWidget = () => {
    console.log("iw")
    new window.TradingView.widget({
      autosize: true,
      symbol: this.symbol(),
      interval: "240",
      timezone: "UTC",
      theme: "Dark",
      style: "1",
      locale: "en",
      toolbar_bg: "#f1f3f6",
      enable_publishing: false,
      withdateranges: false,
      save_image: true,
      show_popup_button: true,
      popup_width: "1000",
      popup_height: "650",
      container_id: CONTAINER_ID
    })
  }

  symbol = () => {
    var exchange = this.props.coin.exchange.toUpperCase()
    if (exchange === "GDAX") {
      exchange = "COINBASE"
    }
    return exchange + ":" + this.props.coin.base + this.props.coin.counter
  }

  render() {
    const ChartOuter = styled.div`
      overflow: hidden;
      height: calc(100% - 34px);
    `

    const ChartInner = styled.div`
      margin-top: -1px;
      margin-left: -8px;
      margin-right: -8px;
      margin-bottom: 0;
      height: calc(100% + 8px);
    `

    return (
      <ChartOuter>
        <ChartInner id={CONTAINER_ID} />
      </ChartOuter>
    )
  }
}

const Chart = ({coin}) => {

  if (!coin) {
    return (
      <Section id="chart" heading="Chart">
        <Para>No coin selected</Para>
      </Section>
    )
  }

  if (coin.exchange === "kucoin") {
    return (
      <Section id="chart" heading="Chart" expand>
        <Para>TradingView does not support charts for this exchange.</Para>
        <Para><a target="_blank" href={"https://www.kucoin.com/#/trade.pro/" + coin.base + "-" + coin.counter}>Open in {coin.exchange}</a></Para>
      </Section>
    )
  }

  return (
    <Section id="chart" heading="Chart" expand>
      <ChartContent coin={coin} />
    </Section>
  )
}

export default Chart
