import React from "react"

import { Route } from "react-router-dom"
import { WidthProvider, Responsive } from "react-grid-layout"
import styled from "styled-components"
import { color } from "styled-system"
import { Tab } from "semantic-ui-react"
import theme from "./theme"

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
  height: ${props => (props.height ? props.height + "px" : "auto")}
  box-shadow: 2px 2px 6px rgba(0, 0, 0, .2);
`

const PositioningWrapper = ({ mobile, children }) =>
  mobile ? (
    <div>{children}</div>
  ) : (
    <FloatingPositioningWrapper>{children}</FloatingPositioningWrapper>
  )

const FloatingPositioningWrapper = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
`

const baseLayouts = {
  lg: [
    { i: "coins", x: 0, y: 0, w: 4, h: 25 },
    { i: "jobs", x: 13, y: 25, w: 7, h: 9 },
    { i: "chart", x: 4, y: 0, w: 9, h: 21 },
    { i: "openOrders", x: 13, y: 11, w: 7, h: 14 },
    { i: "balance", x: 4, y: 20, w: 9, h: 4 },
    { i: "tradeSelector", x: 6, y: 25, w: 7, h: 9 },
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
      isMobile: window.innerWidth <= 500,
      layouts: originalLayouts
    }
  }

  componentWillMount() {
    window.addEventListener("resize", this.handleWindowSizeChange)
  }

  handleWindowSizeChange = () => {
    const isMobile = window.innerWidth <= 500
    if (isMobile !== this.state.isMobile) this.setState({ isMobile })
  }

  resetLayout = () => {
    saveToLS("layouts", baseLayouts)
    this.setState({ layouts: baseLayouts })
  }

  onLayoutChange = (layout, layouts) => {
    saveToLS("layouts", layouts)
    this.setState({ layouts })
  }

  render() {
    const { isMobile } = this.state

    const Tools = ({ coin }) => (
      <ToolbarContainer
        coin={coin}
        mobile={isMobile}
        onResetLayout={this.resetLayout}
      />
    )
    const Market = ({ coin }) => (
      <MarketContainer coin={coin} allowAnimate={!isMobile} />
    )
    const ManageAlerts = ({ coin }) => (
      <ManageAlertsContainer coin={coin} mobile={isMobile} />
    )

    const header = [
      <WithCoinParameter key="toolbar" component={Tools} />,
      <Route
        key="addCoin"
        exact
        path="/addCoin"
        component={AddCoinContainer}
      />,
      <Route key="job" path="/job/:jobId" component={JobContainer} />,
      <PositioningWrapper key="dialogs" mobile={isMobile}>
        <WithCoinParameter key="managealerts" component={ManageAlerts} />
        <SetReferencePriceContainer key="setreferenceprice" mobile={isMobile} />
      </PositioningWrapper>
    ]

    if (isMobile) {
      return (
        <div style={{ height: "100%" }}>
          {header}
          <Tab
            menu={{ inverted: true, color: "blue" }}
            panes={[
              { menuItem: "Coins", render: () => <CoinsContainer /> },
              {
                menuItem: "Chart",
                render: () => (
                  <div style={{ height: "400px" }}>
                    <WithCoinParameter component={Chart} />
                  </div>
                )
              },
              {
                menuItem: "Book",
                render: () => <WithCoinParameter component={Market} />
              },
              {
                menuItem: "Trading",
                render: () => (
                  <div>
                    <WithCoinParameter component={BalanceContainer} />
                    <WithCoinParameter component={TradingContainer} />
                  </div>
                )
              },
              {
                menuItem: "Orders",
                render: () => <WithCoinParameter component={OrdersContainer} />
              },
              {
                menuItem: "Status",
                render: () => (
                  <div>
                    <NotificationsContainer />
                    <JobsContainer />
                  </div>
                )
              }
            ]}
          />
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
            onLayoutChange={this.onLayoutChange}
            margin={[theme.space[1], theme.space[1]]}
            containerPadding={[theme.space[1], theme.space[1]]}
            draggableHandle=".dragMe"
          >
            <LayoutBox key="chart" bg="backgrounds.1" expand height={300}>
              <WithCoinParameter component={Chart} />
            </LayoutBox>
            <LayoutBox key="openOrders" bg="backgrounds.1">
              <WithCoinParameter component={OrdersContainer} />
            </LayoutBox>
            <LayoutBox key="balance" bg="backgrounds.1">
              <WithCoinParameter component={BalanceContainer} />
            </LayoutBox>
            <LayoutBox key="tradeSelector" bg="backgrounds.1" expand>
              <WithCoinParameter component={TradingContainer} />
            </LayoutBox>
            <LayoutBox key="coins" bg="backgrounds.1">
              <CoinsContainer />
            </LayoutBox>
            <LayoutBox key="jobs" bg="backgrounds.1">
              <JobsContainer />
            </LayoutBox>
            <LayoutBox key="marketData" bg="backgrounds.1">
              <WithCoinParameter component={Market} />
            </LayoutBox>
            <LayoutBox key="notifications" bg="backgrounds.1">
              <NotificationsContainer />
            </LayoutBox>
          </ResponsiveReactGridLayout>
        </div>
      )
    }
  }
}
