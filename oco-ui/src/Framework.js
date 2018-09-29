import React from "react"

import { Route } from "react-router-dom"
import { WidthProvider, Responsive } from "react-grid-layout"
import styled from "styled-components"
import { color } from "styled-system"

import { getFromLS, saveToLS } from "./util/localStorage"

import CoinsContainer from "./containers/CoinsContainer"
import JobContainer from "./containers/JobContainer"
import JobsContainer from "./containers/JobsContainer"
import ToolbarContainer from "./containers/ToolbarContainer"
import AddCoinContainer from "./containers/AddCoinContainer"
import MarketContainer from "./containers/MarketContainer"
import OrdersContainer from "./containers/OrdersContainer"
import TradingContainer from "./containers/TradingContainer"
import BalanceContainer from "./containers/BalanceContainer"
import NotificationsContainer from "./containers/NotificationsContainer"
import ManageAlertsContainer from "./containers/ManageAlertsContainer"
import SetReferencePriceContainer from "./containers/SetReferencePriceContainer"
import Chart from "./components/Chart"

import WithCoinParameter from "./WithCoinParameter"

const ResponsiveReactGridLayout = WidthProvider(Responsive)

const LayoutBox = styled.div`
  ${color}
  height: ${props => props.height ? props.height + "px" : "auto"}
`

const baseLayouts = {
  lg: [
    { i: "coins", x: 0, y: 0, w: 4, h: 25 },
    { i: "jobs", x: 13, y: 25, w: 7, h: 9 },
    { i: "chart", x: 4, y: 0, w: 9, h: 21 },
    { i: "openOrders", x: 13, y: 11, w: 7, h: 14 },
    { i: "balance", x: 4, y: 20, w: 9, h: 4 },
    { i: "tradeSelector", x: 6, y: 25, w: 7, h: 9},
    { i: "marketData", x: 13, y: 0, w: 7, h: 11 },
    { i: "notifications", x: 0, y: 25, w: 6, h: 9 }
  ],
  md: [
    { i: "chart", x: 0, y: 100, w: 5, h: 13 },
    { i: "openOrders", x: 0, y: 200, w: 5, h: 6 },
    { i: "balance", x: 0, y: 300, w: 5, h: 4 },
    { i: "tradeSelector", x: 0, y: 400, w: 5, h: 9 },
    { i: "coins", x: 5, y: 100, w: 3, h: 12 },
    { i: "jobs", x: 5, y: 200, w: 3, h: 9 },
    { i: "marketData", x: 5, y: 300, w: 3, h: 4 },
    { i: "notifications", x: 5, y: 400, w: 3, h: 7 }
  ],
  sm: [
    { i: "chart", x: 0, y: 100, w: 2, h: 12 },
    { i: "openOrders", x: 0, y: 200, w: 2, h: 6 },
    { i: "balance", x: 0, y: 300, w: 2, h: 4 },
    { i: "tradeSelector", x: 0, y: 400, w: 2, h: 9 },
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
      <Route key="job" path="/job/:jobId" component={JobContainer} />,
    ]

    const content = [
      <LayoutBox key="chart" bg="backgrounds.1" expand height={300}>
        <WithCoinParameter component={Chart}/>
      </LayoutBox>,
      <LayoutBox key="openOrders" bg="backgrounds.1">
        <WithCoinParameter component={OrdersContainer}/>
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

    const ManageAlertsActual = ({coin}) => <ManageAlertsContainer coin={coin} mobile={isMobile}/>
    const footer = [
      <WithCoinParameter key="managealerts" component={ManageAlertsActual}/>,
      <SetReferencePriceContainer key="setrefprice"/>
    ]

    if (isMobile) {
      return (
        <div style={{ position: "relative" }}>
          {header}
          {content}
          {footer}
        </div>
      )
    } else {
      return (
        <div>
          {header}
          <ResponsiveReactGridLayout
            breakpoints={{ lg: 1630, md: 900, sm: 0 }}
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
          {footer}
        </div>
      )
    }
  }
}