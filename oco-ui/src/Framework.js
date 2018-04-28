import React from "react"

import { Switch, Route, BrowserRouter } from "react-router-dom"
import { WidthProvider, Responsive } from "react-grid-layout"
import styled from "styled-components"
import { color } from "styled-system"

import CoinsContainer from "./containers/CoinsContainer"
import JobContainer from "./containers/JobContainer"
import JobsContainer from "./containers/JobsContainer"
import ToolbarContainer from "./containers/ToolbarContainer"
import AddCoinContainer from "./containers/AddCoinContainer"
import CoinInfoContainer from "./containers/CoinInfoContainer"
import MarketContainer from "./containers/MarketContainer"
import OpenOrdersContainer from "./containers/OpenOrdersContainer"
import TradeSelector from "./components/TradeSelector"
import Chart from "./components/Chart"

import WithCoinParameter from "./WithCoinParameter"

const ResponsiveReactGridLayout = WidthProvider(Responsive)

const MarketContainerProvider = props => (
  <WithCoinParameter {...props} component={MarketContainer} />
)
const OpenOrdersContainerProvider = props => (
  <WithCoinParameter {...props} component={OpenOrdersContainer} />
)
const CoinInfoContainerProvider = props => (
  <WithCoinParameter {...props} component={CoinInfoContainer} />
)
const TradeSelectorProvider = props => (
  <WithCoinParameter {...props} component={TradeSelector} />
)
const ChartProvider = props => (
  <WithCoinParameter {...props} component={Chart} />
)

const LayoutBox = styled.div`
  ${color}
  border: 1px solid ${props => props.theme.colors.boxBorder};
  overflow: ${props => props.scroll ? "scroll" : "hidden"};
  ::-webkit-scrollbar-corner {
    background: ${props => props.theme.colors.boxBorder};
  }
  ::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  } 
`

const originalLayouts = getFromLS("layouts") || {
  lg: [
    { i: "coins",         x: 0, y: 0, w: 5, h: 15, minH: 5, minW: 6 },
    { i: "jobs",          x: 0, y: 100, w: 5, h: 15, minH: 5, minW: 6 },
    { i: "coinInfo",      x: 5, y: 0, w: 10, h: 5, minH: 5, minW: 10 },
    { i: "chart",         x: 5, y: 100, w: 10, h: 20, minH: 4 },
    { i: "tradeSelector", x: 5, y: 200, w: 10, h: 13, minH: 13, maxH: 13 },
    { i: "openOrders",    x: 15, y: 0, w: 5, h: 10, minH: 4, minW: 5 },
    { i: "marketData",    x: 15, y: 100, w: 5, h: 10, minH: 6, minW: 5  },
  ],
  md: [
    { i: "coinInfo",      x: 0, y: 0, w: 5, h: 5, minH: 5, minW: 5, maxH: 8},
    { i: "chart",         x: 0, y: 100, w: 5, h: 16, minH: 4 },
    { i: "tradeSelector", x: 0, y: 200, w: 5, h: 13, minH: 13, maxH: 13 },
    { i: "coins",         x: 5, y: 0, w: 3, h: 12, minH: 5, minW: 3},
    { i: "openOrders",    x: 5, y: 100, w: 3, h: 8, minH: 4, minW: 3 },
    { i: "jobs",          x: 5, y: 200, w: 3, h: 8, minH: 4, minW: 3 },
    { i: "marketData",    x: 5, y: 300, w: 3, h: 6, minH: 6, minW: 3 },
  ],
  sm: [
    { i: "coinInfo",      x: 0, y: 0, w: 2, h: 8 },
    { i: "chart",         x: 0, y: 100, w: 2, h: 16 },
    { i: "tradeSelector", x: 0, y: 200, w: 2, h: 13 },
    { i: "coins",         x: 0, y: 300, w: 2, h: 12 },
    { i: "openOrders",    x: 0, y: 400, w: 2, h: 8 },
    { i: "jobs",          x: 0, y: 500, w: 2, h: 8 },
    { i: "marketData",    x: 0, y: 600, w: 2, h: 6 },
  ]
};

export default class Framework extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      layouts: JSON.parse(JSON.stringify(originalLayouts))
    };
  }

  resetLayout() {
    this.setState({ layouts: {} });
  }

  onLayoutChange(layout, layouts) {
    saveToLS("layouts", layouts);
    this.setState({ layouts });
  }

  render() {
    return (
      <BrowserRouter>
        <div>
          <Switch>
            <Route
              path="/coin/:exchange/:counter/:base"
              component={ToolbarContainer}
            />
            <Route component={ToolbarContainer} />
          </Switch>
          <ResponsiveReactGridLayout 
            breakpoints={{ lg: 1400, md: 850, sm: 0 }}
            cols={{ lg: 20, md: 8, sm: 2 }}
            rowHeight={24}
            layouts={this.state.layouts}
            onLayoutChange={(layout, layouts) =>
              this.onLayoutChange(layout, layouts)
            }
            margin={[0, 0]}
            containerPadding={[0, 0]}
          >
            <LayoutBox key="coinInfo" bg="backgrounds.2">
              <Switch>
                <Route exact path="/addCoin" component={AddCoinContainer} />
                <Route
                  path="/coin/:exchange/:counter/:base"
                  component={CoinInfoContainerProvider}
                />
                <Route path="/job/:jobId" component={JobContainer} />
              </Switch>
            </LayoutBox>
            <LayoutBox key="chart" bg="backgrounds.1">
              <Switch>
                <Route
                  path="/coin/:exchange/:counter/:base"
                  component={ChartProvider}
                />
              </Switch>
            </LayoutBox>
            <LayoutBox key="tradeSelector" bg="backgrounds.1">
              <Switch>
                <Route
                  path="/coin/:exchange/:counter/:base"
                  component={TradeSelectorProvider}
                />
              </Switch>
            </LayoutBox>
            <LayoutBox key="coins" scroll bg="backgrounds.1">
              <CoinsContainer />
            </LayoutBox>
            <LayoutBox key="marketData" scroll bg="backgrounds.2">
              <Switch>
                <Route
                  path="/coin/:exchange/:counter/:base"
                  component={MarketContainerProvider}
                />
              </Switch>
            </LayoutBox>
            <LayoutBox key="openOrders" scroll bg="backgrounds.3">
              <Switch>
                <Route
                  path="/coin/:exchange/:counter/:base"
                  component={OpenOrdersContainerProvider}
                />
              </Switch>
            </LayoutBox>
            <LayoutBox key="jobs" scroll bg="backgrounds.2">
              <JobsContainer />
            </LayoutBox>
          </ResponsiveReactGridLayout>
        </div>
      </BrowserRouter>
    )
  }
}

function getFromLS(key) {
  let ls = null;
  if (global.localStorage) {
    try {
      ls = JSON.parse(global.localStorage.getItem(key)) || null;
    } catch (e) {
      /*Ignore*/
    }
  }
  return ls;
}

function saveToLS(key, value) {
  if (global.localStorage) {
    global.localStorage.setItem(key, JSON.stringify(value));
  }
}