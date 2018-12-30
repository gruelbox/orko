import React, { Component } from "react"
import styled from "styled-components"

import Section from "./primitives/Section"
import Para from "./primitives/Para"
import Tab from "./primitives/Tab"
import Span from "./primitives/Span"

const CHART_INTERVAL_KEY = "Chart.interval"

const CONTAINER_ID = "tradingview-widget-container"

const NewWindowChartContent = ({ coin, url }) => (
  <Section id="chart" heading="Chart">
    <Para>TradingView does not support charts for this exchange.</Para>
    <Para>
      <a target="_blank" rel="noopener noreferrer" href={url}>
        Open in {coin.exchange}
      </a>
    </Para>
  </Section>
)

const ChartOuter = styled.div`
  overflow: hidden;
  height: 100%;
`

const ChartInner = styled.div`
  margin-top: -1px;
  margin-left: -8px;
  margin-right: -8px;
  margin-bottom: 0;
  height: calc(100% + 8px);
`

class TradingViewChartContent extends Component {
  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return (
      this.props.coin.key !== nextProps.coin.key ||
      this.props.interval !== nextProps.interval
    )
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
    new window.TradingView.widget({
      autosize: true,
      symbol: this.symbol(),
      interval: this.props.interval,
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
      container_id: CONTAINER_ID,
      hide_side_toolbar: false,
      studies: []
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
    return (
      <ChartOuter>
        <ChartInner id={CONTAINER_ID} />
      </ChartOuter>
    )
  }
}

class Chart extends React.Component {
  constructor(props) {
    super(props)
    var interval = localStorage.getItem(CHART_INTERVAL_KEY)
    if (!interval) {
      interval = "240"
    }
    this.state = { interval }
  }

  render() {
    const coin = this.props.coin

    if (!coin) {
      return (
        <Section id="chart" heading="Chart">
          <Para>No coin selected</Para>
        </Section>
      )
    }

    if (coin.exchange === "kucoin") {
      return (
        <NewWindowChartContent
          coin={coin}
          url={
            "https://www.kucoin.com/#/trade.pro/" +
            coin.base +
            "-" +
            coin.counter
          }
        />
      )
    }

    if (coin.exchange === "cryptopia") {
      return (
        <NewWindowChartContent
          coin={coin}
          url={
            "https://www.cryptopia.co.nz/Exchange/?market=" +
            coin.base +
            "_" +
            coin.counter
          }
        />
      )
    }

    const Interval = ({ name, code, selected }) => (
      <Tab
        selected={selected === code}
        onClick={() => {
          localStorage.setItem(CHART_INTERVAL_KEY, code)
          this.setState({ interval: code })
        }}
      >
        {name}
      </Tab>
    )

    return (
      <Section
        id="chart"
        heading="Chart"
        expand
        buttons={() => (
          <span>
            <Span>Default interval</Span>
            <Interval selected={this.state.interval} code="W" name="W" />
            <Interval selected={this.state.interval} code="D" name="D" />
            <Interval selected={this.state.interval} code="240" name="4h" />
            <Interval selected={this.state.interval} code="60" name="1h" />
            <Interval selected={this.state.interval} code="15" name="15m" />
          </span>
        )}
      >
        <TradingViewChartContent coin={coin} interval={this.state.interval} />
      </Section>
    )
  }
}

export default Chart
