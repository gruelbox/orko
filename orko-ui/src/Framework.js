import React from "react"

import { Route } from "react-router-dom"
import { WidthProvider, Responsive } from "react-grid-layout"
import styled from "styled-components"
import { color } from "styled-system"
import { Tab } from "semantic-ui-react"
import theme from "./theme"

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

export default ({
  isMobile,
  panels,
  layouts,
  showSettings,
  onToggleViewSettings,
  onHidePanel,
  onChangePanels,
  onResetLayout,
  onLayoutChange
}) => {
  const Tools = () => (
    <ToolbarContainer
      mobile={isMobile}
      onShowViewSettings={onToggleViewSettings}
      panels={panels}
    />
  )
  const Market = () => <MarketContainer allowAnimate={!isMobile} />
  const ManageAlerts = () => <ManageAlertsContainer mobile={isMobile} />

  const Settings = () =>
    showSettings ? (
      <ViewSettings
        panels={panels}
        onChangePanels={onChangePanels}
        onClose={onToggleViewSettings}
        onReset={onResetLayout}
      />
    ) : (
      <React.Fragment />
    )

  const ManageScripts = props => (
    <ManageScriptsContainer {...props} key={props.match.params.id} />
  )

  const header = [
    <Tools key="tools" />,
    <Route key="addCoin" exact path="/addCoin" component={AddCoinContainer} />,
    <Route key="scriptsNoId" exact path="/scripts" component={ManageScripts} />,
    <Route key="scripts" exact path="/scripts/:id" component={ManageScripts} />,
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
        <ChartContainer onHide={() => onHidePanel("chart")} />
      </LayoutBox>
    ),
    openOrders: () => (
      <LayoutBox key="openOrders" bg="backgrounds.1">
        <OrdersContainer onHide={() => onHidePanel("openOrders")} />
      </LayoutBox>
    ),
    balance: () => (
      <LayoutBox key="balance" bg="backgrounds.1">
        <BalanceContainer onHide={() => onHidePanel("balance")} />
      </LayoutBox>
    ),
    tradeSelector: () => (
      <LayoutBox key="tradeSelector" bg="backgrounds.1" expand>
        <TradingContainer onHide={() => onHidePanel("tradeSelector")} />
      </LayoutBox>
    ),
    coins: () => (
      <LayoutBox key="coins" bg="backgrounds.1">
        <CoinsContainer onHide={() => onHidePanel("coins")} />
      </LayoutBox>
    ),
    jobs: () => (
      <LayoutBox key="jobs" bg="backgrounds.1">
        <JobsContainer onHide={() => onHidePanel("jobs")} />
      </LayoutBox>
    ),
    marketData: () => (
      <LayoutBox key="marketData" bg="backgrounds.1">
        <Market onHide={() => onHidePanel("marketData")} />
      </LayoutBox>
    ),
    notifications: () => (
      <LayoutBox key="notifications" bg="backgrounds.1">
        <NotificationsContainer onHide={() => onHidePanel("notifications")} />
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
                  <ChartContainer />
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
          layouts={layouts.asMutable()}
          onLayoutChange={onLayoutChange}
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
