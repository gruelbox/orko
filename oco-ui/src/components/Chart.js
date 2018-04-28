import React, { Component } from "react"
import styled from "styled-components"

import Section from "./primitives/Section"
import Para from "./primitives/Para"

const SCRIPT_ID = "tradingview-widget-script"
const CONTAINER_ID = "tradingview-widget-container"

class ChartContent extends Component {
  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return this.props.coin.key !== nextProps.coin.key
  }

  componentDidMount = () => {
    this.appendScript(this.initWidget)
  }

  appendScript = onload => {
    if (this.scriptExists()) {
      /* global TradingView */
      if (typeof TradingView === "undefined") {
        this.updateOnloadListener(onload)
        return
      }
      onload()
      return
    }
    const script = document.createElement("script")
    script.id = SCRIPT_ID
    script.type = "text/javascript"
    script.async = true
    script.src = "https://s3.tradingview.com/tv.js"
    script.onload = onload
    document.getElementsByTagName("head")[0].appendChild(script)
  }

  getScriptElement = () => document.getElementById(SCRIPT_ID)

  scriptExists = () => this.getScriptElement() !== null

  updateOnloadListener = onload => {
    const script = this.getScriptElement()
    const oldOnload = script.onload
    return (script.onload = () => {
      oldOnload()
      onload()
    })
  }

  componentDidUpdate = () => {
    document.getElementById(CONTAINER_ID).innerHTML = ""
    this.initWidget()
  }

  initWidget = () => {
    new TradingView.widget({
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
    const ChartContainer = styled.div`
      overflow: hidden;
      height: 100%;
    `

    const ChartInner = styled.div`
      margin-top: -1px;
      margin-left: -8px;
      margin-right: -8px;
      margin-bottom: -8px;
      height: calc(100% - 36px);
    `

    return (
      <ChartContainer>
        <ChartInner id={CONTAINER_ID} />
      </ChartContainer>
    )
  }
}

const Chart = props => {
  if (props.coin && props.coin.exchange !== "kucoin") {
    return (
      <Section id="chart" heading="Chart" expand>
        <ChartContent coin={props.coin} />
      </Section>
    )
  } else {
    return (
      <Section id="chart" heading="Chart">
        <Para>TradingView does not support charts for this exchange.</Para>
      </Section>
    )
  }
}

export default Chart
