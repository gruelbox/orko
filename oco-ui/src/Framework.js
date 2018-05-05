import React from "react"

import { Route, BrowserRouter } from "react-router-dom"
import { WidthProvider, Responsive } from "react-grid-layout"
import styled from "styled-components"
import { color } from "styled-system"

import CoinsContainer from "./containers/CoinsContainer"
import JobContainer from "./containers/JobContainer"
import JobsContainer from "./containers/JobsContainer"
import ToolbarContainer from "./containers/ToolbarContainer"
import AddCoinContainer from "./containers/AddCoinContainer"
import MarketContainer from "./containers/MarketContainer"
import OpenOrdersContainer from "./containers/OpenOrdersContainer"
import TradingContainer from "./containers/TradingContainer"
import BalanceContainer from "./containers/BalanceContainer"
import NotificationsContainer from "./containers/NotificationsContainer"
import Chart from "./components/Chart"

import WithCoinParameter from "./WithCoinParameter"

const ResponsiveReactGridLayout = WidthProvider(Responsive)

const LayoutBox = styled.div`
  ${color}
  height: ${props => props.height ? props.height + "px" : "auto"}
  position: relative;
  overflow: ${props => props.expand ? "hidden" : "auto"}
  ::-webkit-scrollbar-corner {
    background: ${props => props.theme.colors.boxBorder};
  }
  ::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  ::-webkit-scrollbar-thumb {
    background: ${props => props.theme.colors.deemphasis};
  }
`

const baseLayouts = {
  lg: [
    { i: "ticker", x: 0, y: 0, w: 20, h: 3 },
    { i: "coins", x: 0, y: 0, w: 5, h: 15 },
    { i: "jobs", x: 0, y: 100, w: 5, h: 15 },
    { i: "chart", x: 5, y: 100, w: 10, h: 14 },
    { i: "openOrders", x: 5, y: 200, w: 10, h: 5 },
    { i: "balance", x: 5, y: 300, w: 10, h: 5 },
    { i: "tradeSelector", x: 5, y: 400, w: 10, h: 8},
    { i: "marketData", x: 15, y: 200, w: 5, h: 10 },
    { i: "notifications", x: 15, y: 300, w: 5, h: 10 }
  ],
  md: [
    { i: "ticker", x: 0, y: 0, w: 8, h: 3 },
    { i: "chart", x: 0, y: 100, w: 5, h: 14 },
    { i: "openOrders", x: 0, y: 200, w: 5, h: 5 },
    { i: "balance", x: 0, y: 300, w: 5, h: 5 },
    { i: "tradeSelector", x: 0, y: 400, w: 5, h: 8 },
    { i: "coins", x: 5, y: 100, w: 3, h: 12 },
    { i: "jobs", x: 5, y: 200, w: 3, h: 9 },
    { i: "marketData", x: 5, y: 300, w: 3, h: 4 },
    { i: "notifications", x: 5, y: 400, w: 3, h: 7 }
  ],
  sm: [
    { i: "ticker", x: 0, y: 0, w: 2, h: 3 },
    { i: "chart", x: 0, y: 100, w: 2, h: 12 },
    { i: "openOrders", x: 0, y: 200, w: 2, h: 4 },
    { i: "balance", x: 0, y: 300, w: 2, h: 5 },
    { i: "tradeSelector", x: 0, y: 400, w: 2, h: 8 },
    { i: "coins", x: 0, y: 500, w: 2, h: 6 },
    { i: "jobs", x: 0, y: 600, w: 2, h: 6 },
    { i: "marketData", x: 0, y: 700, w: 2, h: 6 },
    { i: "notifications", x: 0, y: 800, w: 2, h: 6 }
  ]
}

const originalLayouts = getFromLS("layouts") || baseLayouts

export default class Framework extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      width: window.innerWidth,
      layouts: JSON.parse(JSON.stringify(originalLayouts))
    }
  }

  componentWillMount() {
    window.addEventListener('resize', this.handleWindowSizeChange);
  }

  handleWindowSizeChange = () => {
    this.setState({ width: window.innerWidth });
  };

  resetLayout = () => {
    saveToLS("layouts", baseLayouts)
    this.setState({ layouts: baseLayouts })
  }

  onLayoutChange(layout, layouts) {
    saveToLS("layouts", layouts)
    this.setState({ layouts })
  }

  render() {
    const { width } = this.state
    const isMobile = width <= 500

    const Tools = ({coin}) => <ToolbarContainer coin={coin} onResetLayout={this.resetLayout}/>

    const header = [
      <WithCoinParameter key="toolbar" component={Tools}/>,
      <Route key="addCoin" exact path="/addCoin" component={AddCoinContainer} />,
      <Route key="job" path="/job/:jobId" component={JobContainer} />
    ]

    const content = [
      <LayoutBox key="chart" bg="backgrounds.1" expand height={300}>
        <WithCoinParameter component={Chart}/>
      </LayoutBox>,
      <LayoutBox key="openOrders" bg="backgrounds.1">
        <WithCoinParameter component={OpenOrdersContainer}/>
      </LayoutBox>,
      <LayoutBox key="balance" bg="backgrounds.1">
        <WithCoinParameter component={BalanceContainer}/>
      </LayoutBox>,
      <LayoutBox key="tradeSelector" bg="backgrounds.1" expand>
        <WithCoinParameter component={TradingContainer}/>
      </LayoutBox>,
      <LayoutBox key="coins" bg="backgrounds.1">
        <CoinsContainer />
      </LayoutBox>,
      <LayoutBox key="jobs" bg="backgrounds.1">
        <JobsContainer />
      </LayoutBox>,
      <LayoutBox key="marketData" bg="backgrounds.1">
        <WithCoinParameter component={MarketContainer}/>
      </LayoutBox>,
      <LayoutBox key="notifications" bg="backgrounds.1">
        <NotificationsContainer/>
      </LayoutBox>
    ]

    if (isMobile) {
      return (
        <BrowserRouter>
          <div>
            {header}
            {content}
          </div>
        </BrowserRouter>
      )
    } else {
      return (
        <BrowserRouter>
          <div>
            {header}
            <ResponsiveReactGridLayout
              breakpoints={{ lg: 1400, md: 850, sm: 0 }}
              cols={{ lg: 20, md: 8, sm: 2 }}
              rowHeight={24}
              layouts={this.state.layouts}
              onLayoutChange={(layout, layouts) =>
                this.onLayoutChange(layout, layouts)
              }
              margin={[2, 2]}
              containerPadding={[2, 2]}
              draggableHandle=".dragMe"
            >
              {content}
            </ResponsiveReactGridLayout>
          </div>
        </BrowserRouter>
      )
    }
  }
}

function getFromLS(key) {
  let ls = null
  if (global.localStorage) {
    try {
      ls = JSON.parse(global.localStorage.getItem(key)) || null
    } catch (e) {
      /*Ignore*/
    }
  }
  return ls
}

function saveToLS(key, value) {
  if (global.localStorage) {
    global.localStorage.setItem(key, JSON.stringify(value))
  }
}
