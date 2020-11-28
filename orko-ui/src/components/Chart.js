/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React, { Component, useEffect, useMemo, useState } from "react"
import styled from "styled-components"

import Section from "./primitives/Section"
import Para from "./primitives/Para"
import Tab from "./primitives/Tab"
import WithCoin from "../containers/WithCoin"

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

const SimulatedExchangeContent = () => (
  <>
    <Para>Charts not supported for the simulated exchange.</Para>
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
    try {
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
    } catch (error) {
      console.error("Failed to initialise TradingView widget", error)
    }
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

const useSelectedInterval = () => {

  const [interval, setRawInterval] = useState()

  useEffect(() => {
    var fetched = localStorage.getItem(CHART_INTERVAL_KEY)
    if (!fetched) {
      fetched = "240"
    }
    setRawInterval(fetched)
  }, [setRawInterval])

  const setInterval = useMemo(() => newValue => {
    localStorage.setItem(CHART_INTERVAL_KEY, newValue)
    setRawInterval(newValue)
  }, [setRawInterval])

  return [interval, setInterval]
}

const Chart = () => {

  const [interval, setInterval] = useSelectedInterval()

  const Interval = ({ name, code, description }) => (
    <Tab
      selected={interval === code}
      onClick={() => {
        setInterval(code)
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

  return (
    <Section id="chart" heading="Chart" buttons={() => <>
      <Interval code="W" name="W" description="weekly" />
      <Interval code="D" name="D" description="daily" />
      <Interval code="240" name="4H" description="4 hour" />
      <Interval code="60" name="1H" description="1 hour" />
      <Interval code="15" name="15m" description="15 minute" />
    </>}>
      <WithCoin>
        {coin => {
          if (coin.exchange === "kucoin") {
            return (
              <NewWindowChartContent
                coin={coin}
                url={
                  "https://www.kucoin.com/trade/" +
                  coin.base +
                  "-" +
                  coin.counter
                }
              />
            )
          } else if (coin.exchange === "simulated") {
            return <SimulatedExchangeContent />
          } else {
            return <TradingViewChartContent coin={coin} interval={interval} />
          }
        }}
      </WithCoin>
    </Section>
  )
}

export default Chart
