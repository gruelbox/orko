import React from "react"

import { connect } from "react-redux"
import { Route } from "react-router-dom"
import { WidthProvider, Responsive } from "react-grid-layout"
import styled from "styled-components"
import { color } from "styled-system"
import { Tab } from "semantic-ui-react"
import theme from "./theme"
import Immutable from "seamless-immutable"

import * as uiActions from "./store/ui/actions"

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
import ManageScriptsContainer from "./containers/ManageScriptsContainer"
import SetReferencePriceContainer from "./containers/SetReferencePriceContainer"
import ChartContainer from "./containers/ChartContainer"
import ViewSettings from "./components/ViewSettings"

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
  top: 50px;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  padding: ${props => props.theme.space[2] + "px"};
  > * {
    margin: ${props => props.theme.space[2] + "px"};
  }
`

const baseLayouts = Immutable({
  lg: [
    { i: "coins", x: 0, y: 100, w: 4, h: 22 },
    { i: "notifications", x: 0, y: 200, w: 4, h: 9 },
    { i: "chart", x: 4, y: 100, w: 9, h: 18 },
    { i: "balance", x: 4, y: 200, w: 9, h: 4 },
    { i: "tradeSelector", x: 4, y: 300, w: 9, h: 9 },
    { i: "marketData", x: 13, y: 100, w: 7, h: 11 },
    { i: "openOrders", x: 13, y: 200, w: 7, h: 11 },
    { i: "jobs", x: 13, y: 300, w: 7, h: 9 }
  ],
  md: [
    { i: "chart", x: 0, y: 100, w: 10, h: 13 },
    { i: "openOrders", x: 0, y: 200, w: 10, h: 5 },
    { i: "balance", x: 0, y: 300, w: 10, h: 4 },
    { i: "tradeSelector", x: 0, y: 400, w: 10, h: 9 },

    { i: "coins", x: 10, y: 100, w: 6, h: 11 },
    { i: "marketData", x: 10, y: 200, w: 6, h: 8 },
    { i: "jobs", x: 10, y: 300, w: 6, h: 5 },
    { i: "notifications", x: 10, y: 400, w: 6, h: 7 }
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
})

class Framework extends React.Component {
  constructor(props) {
    super(props)
    const loadedLayouts = getFromLS("layouts")
    this.state = {
      isMobile: window.innerWidth <= 500,
      layouts:
        loadedLayouts === null
          ? baseLayouts
          : Immutable.merge(baseLayouts, loadedLayouts),
      showSettings: false
    }
  }

  componentWillMount() {
    window.addEventListener("resize", this.handleWindowSizeChange)
  }

  handleWindowSizeChange = () => {
    const isMobile = window.innerWidth <= 500
    if (isMobile !== this.state.isMobile) this.setState({ isMobile })
  }

  onResetLayout = () => {
    saveToLS("layouts", baseLayouts)
    this.setState({ layouts: baseLayouts })
    this.props.dispatch(uiActions.resetPanels())
  }

  onHidePanel = id => {
    this.props.dispatch(
      uiActions.changePanels(Immutable([{ key: id, visible: false }]))
    )
  }

  onLayoutChange = (layout, layouts) => {
    saveToLS("layouts", layouts)
    this.setState({ layouts: Immutable.merge(baseLayouts, layouts) })
  }

  onChangePanels = panels => {
    this.props.dispatch(uiActions.changePanels(panels))
  }

  onToggleViewSettings = () => {
    this.setState(state => ({ showSettings: !state.showSettings }))
  }

  render() {
    const { isMobile } = this.state
    const { panels } = this.props

    const Tools = () => (
      <ToolbarContainer
        mobile={isMobile}
        onShowViewSettings={this.onToggleViewSettings}
        panels={panels}
      />
    )
    const Market = () => (
      <MarketContainer
        allowAnimate={!isMobile}
        onHide={() => this.onHidePanel("marketData")}
      />
    )
    const ManageAlerts = () => <ManageAlertsContainer mobile={isMobile} />

    const Settings = () =>
      this.state.showSettings ? (
        <ViewSettings
          panels={panels}
          onChangePanels={this.onChangePanels}
          onClose={this.onToggleViewSettings}
          onReset={this.onResetLayout}
        />
      ) : (
        <React.Fragment />
      )

    const ManageScripts = props => (
      <ManageScriptsContainer {...props} key={props.match.params.id} />
    )

    const header = [
      <Tools key="tools" />,
      <Route
        key="addCoin"
        exact
        path="/addCoin"
        component={AddCoinContainer}
      />,
      <Route
        key="scriptsNoId"
        exact
        path="/scripts"
        component={ManageScripts}
      />,
      <Route
        key="scripts"
        exact
        path="/scripts/:id"
        component={ManageScripts}
      />,
      <Route key="job" path="/job/:jobId" component={JobContainer} />,
      <PositioningWrapper key="dialogs" mobile={isMobile}>
        <Settings />
        <ManageAlerts />
        <SetReferencePriceContainer key="setreferenceprice" mobile={isMobile} />
      </PositioningWrapper>
    ]

    const panelsRenderers = {
      chart: () => (
        <LayoutBox key="chart" bg="backgrounds.1" expand height={300}>
          <ChartContainer onHide={() => this.onHidePanel("chart")} />
        </LayoutBox>
      ),
      openOrders: () => (
        <LayoutBox key="openOrders" bg="backgrounds.1">
          <OrdersContainer onHide={() => this.onHidePanel("openOrders")} />
        </LayoutBox>
      ),
      balance: () => (
        <LayoutBox key="balance" bg="backgrounds.1">
          <BalanceContainer onHide={() => this.onHidePanel("balance")} />
        </LayoutBox>
      ),
      tradeSelector: () => (
        <LayoutBox key="tradeSelector" bg="backgrounds.1" expand>
          <TradingContainer onHide={() => this.onHidePanel("tradeSelector")} />
        </LayoutBox>
      ),
      coins: () => (
        <LayoutBox key="coins" bg="backgrounds.1">
          <CoinsContainer onHide={() => this.onHidePanel("coins")} />
        </LayoutBox>
      ),
      jobs: () => (
        <LayoutBox key="jobs" bg="backgrounds.1">
          <JobsContainer onHide={() => this.onHidePanel("jobs")} />
        </LayoutBox>
      ),
      marketData: () => (
        <LayoutBox key="marketData" bg="backgrounds.1">
          <Market />
        </LayoutBox>
      ),
      notifications: () => (
        <LayoutBox key="notifications" bg="backgrounds.1">
          <NotificationsContainer
            onHide={() => this.onHidePanel("notifications")}
          />
        </LayoutBox>
      )
    }

    if (isMobile) {
      return (
        <div style={{ height: "100%" }}>
          {header}
          <Tab
            menu={{ inverted: true, color: "blue" }}
            panes={[
              { menuItem: "Coins", render: panelsRenderers.coins },
              {
                menuItem: "Chart",
                render: () => (
                  <LayoutBox key="chart" bg="backgrounds.1" expand height={500}>
                    <ChartContainer onHide={() => this.onHidePanel("chart")} />
                  </LayoutBox>
                )
              },
              {
                menuItem: "Book",
                render: () => <Market />
              },
              {
                menuItem: "Trading",
                render: () => (
                  <React.Fragment>
                    <div style={{ marginBottom: "4px" }}>
                      {panelsRenderers.balance()}
                    </div>
                    {panelsRenderers.tradeSelector()}
                  </React.Fragment>
                )
              },
              { menuItem: "Orders", render: panelsRenderers.openOrders },
              {
                menuItem: "Status",
                render: () => (
                  <div>
                    <div style={{ marginBottom: "4px" }}>
                      {panelsRenderers.notifications()}
                    </div>
                    {panelsRenderers.jobs()}
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
            breakpoints={{ lg: 1630, md: 992, sm: 0 }}
            cols={{ lg: 20, md: 16, sm: 2 }}
            rowHeight={24}
            layouts={this.state.layouts.asMutable()}
            onLayoutChange={this.onLayoutChange}
            margin={[theme.space[1], theme.space[1]]}
            containerPadding={[theme.space[1], theme.space[1]]}
            draggableHandle=".dragMe"
          >
            {panels.filter(p => p.visible).map(p => panelsRenderers[p.key]())}
          </ResponsiveReactGridLayout>
        </div>
      )
    }
  }
}

export default connect(state => ({
  panels: state.ui.panels
}))(Framework)
