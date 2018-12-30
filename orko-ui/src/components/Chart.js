import React, { Component } from "react"
import styled from "styled-components"

import Section from "./primitives/Section"
import Para from "./primitives/Para"
import Tab from "./primitives/Tab"

const CHART_INTERVAL_KEY = "Chart.interval"

const CONTAINER_ID = "tradingview-widget-container"

const NewWindowChartContent = ({ coin, url, onHide }) => (
  <>
    <Para>TradingView does not support charts for this exchange.</Para>
    <Para>
      <a target="_blank" rel="noopener noreferrer" href={url}>
        Open in {coin.exchange}
      </a>
    </Para>
  </>
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
    var content
    var buttons

    const coin = this.props.coin

    if (!coin) {
      content = <Para>No coin selected</Para>
    } else if (coin.exchange === "kucoin") {
      content = (
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
    } else if (coin.exchange === "cryptopia") {
      content = (
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
    } else {
      const Interval = ({ name, code, selected, description }) => (
        <Tab
          selected={selected === code}
          onClick={() => {
            localStorage.setItem(CHART_INTERVAL_KEY, code)
            this.setState({ interval: code })
          }}
          title={
            "Select the " +
            description +
            " chart (this option will be remembered, unlike selecting the interval from the TradingView chart)"
          }
        >
          {name}
        </Tab>
      )
      buttons = () => (
        <span>
          <Interval
            selected={this.state.interval}
            code="W"
            name="W"
            description="weekly"
          />
          <Interval
            selected={this.state.interval}
            code="D"
            name="D"
            description="daily"
          />
          <Interval
            selected={this.state.interval}
            code="240"
            name="4h"
            description="4 hour"
          />
          <Interval
            selected={this.state.interval}
            code="60"
            name="1h"
            description="1 hour"
          />
          <Interval
            selected={this.state.interval}
            code="15"
            name="15m"
            description="15 minute"
          />
        </span>
      )
      content = (
        <TradingViewChartContent coin={coin} interval={this.state.interval} />
      )
    }
    return (
      <Section
        draggable
        id="chart"
        heading="Chart"
        buttons={buttons}
        onHide={this.props.onHide}
      >
        {content}
      </Section>
    )
  }
}

export default Chart
