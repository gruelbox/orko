import React from "react"

import { Route } from "react-router-dom"
import { WidthProvider, Responsive } from "react-grid-layout"
import { Rnd } from "react-rnd"
import styled from "styled-components"
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
import { Provider as SectionProvider } from "./components/primitives/Section"
import { isNull } from "util"

const ResponsiveReactGridLayout = WidthProvider(Responsive)

const LayoutBox = styled.div`
  height: 100%;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.2);
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

export default class Framework extends React.Component {
  panelsRenderers = isNull

  constructor(props) {
    super(props)

    const icons = this.props.panels.reduce(function(accumulator, panel) {
      accumulator[panel.key] = panel.icon
      return accumulator
    }, {})

    const Panel = ({ id, children }) => (
      <SectionProvider
        value={{
          draggable: true,
          icon: icons[id],
          onHide: this.props.isMobile
            ? null
            : () => this.props.onTogglePanelVisible(id),
          onToggleAttached: this.props.isMobile
            ? null
            : () => this.props.onTogglePanelAttached(id)
        }}
      >
        {children}
      </SectionProvider>
    )

    this.panelsRenderers = {
      chart: () => (
        <LayoutBox key="chart" data-grid={this.props.layoutsAsObj.chart}>
          <Panel id="chart">
            <ChartContainer />
          </Panel>
        </LayoutBox>
      ),
      openOrders: () => (
        <LayoutBox
          key="openOrders"
          data-grid={this.props.layoutsAsObj.openOrders}
        >
          <Panel id="openOrders">
            <OrdersContainer />
          </Panel>
        </LayoutBox>
      ),
      balance: () => (
        <LayoutBox key="balance" data-grid={this.props.layoutsAsObj.balance}>
          <Panel id="balance">
            <BalanceContainer />
          </Panel>
        </LayoutBox>
      ),
      tradeSelector: () => (
        <LayoutBox
          key="tradeSelector"
          data-grid={this.props.layoutsAsObj.tradeSelector}
        >
          <Panel id="tradeSelector">
            <TradingContainer />
          </Panel>
        </LayoutBox>
      ),
      coins: () => (
        <LayoutBox key="coins" data-grid={this.props.layoutsAsObj.coins}>
          <Panel id="coins">
            <CoinsContainer />
          </Panel>
        </LayoutBox>
      ),
      jobs: () => (
        <LayoutBox key="jobs" data-grid={this.props.layoutsAsObj.jobs}>
          <Panel id="jobs">
            <JobsContainer />
          </Panel>
        </LayoutBox>
      ),
      marketData: () => (
        <LayoutBox
          key="marketData"
          data-grid={this.props.layoutsAsObj.marketData}
        >
          <Panel id="marketData">
            <MarketContainer allowAnimate={!this.props.isMobile} />
          </Panel>
        </LayoutBox>
      ),
      notifications: () => (
        <LayoutBox
          key="notifications"
          data-grid={this.props.layoutsAsObj.notifications}
        >
          <Panel id="notifications">
            <NotificationsContainer />
          </Panel>
        </LayoutBox>
      )
    }
  }

  render() {
    var {
      isMobile,
      width,
      panels,
      layouts,
      showSettings,
      onToggleViewSettings,
      onTogglePanelVisible,
      onResetLayout,
      onLayoutChange,
      onMovePanel,
      onResizePanel
    } = this.props

    const Settings = () =>
      showSettings ? (
        <ViewSettings
          panels={panels}
          onTogglePanelVisible={onTogglePanelVisible}
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
      <ToolbarContainer
        key="tools"
        mobile={isMobile}
        onShowViewSettings={onToggleViewSettings}
        onTogglePanelVisible={onTogglePanelVisible}
        on
        panels={panels}
        width={width}
      />,
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
        <ManageAlertsContainer mobile={isMobile} />
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
              { menuItem: "Coins", render: this.panelsRenderers.coins },
              {
                menuItem: "Chart",
                render: () => <ChartContainer />
              },
              {
                menuItem: "Book",
                render: () => <MarketContainer allowAnimate={false} />
              },
              {
                menuItem: "Trading",
                render: () => (
                  <React.Fragment>
                    <div style={{ marginBottom: "4px" }}>
                      {this.panelsRenderers.balance()}
                    </div>
                    {this.panelsRenderers.tradeSelector()}
                  </React.Fragment>
                )
              },
              { menuItem: "Orders", render: this.panelsRenderers.openOrders },
              {
                menuItem: "Status",
                render: () => (
                  <div>
                    <div style={{ marginBottom: "4px" }}>
                      {this.panelsRenderers.notifications()}
                    </div>
                    {this.panelsRenderers.jobs()}
                  </div>
                )
              }
            ]}
          />
        </div>
      )
    } else {
      return (
        <>
          {header}
          <div style={{ padding: "-" + theme.space[1] + "px" }}>
            <ResponsiveReactGridLayout
              breakpoints={{ lg: 1630, md: 992, sm: 0 }}
              cols={{ lg: 40, md: 32, sm: 4 }}
              rowHeight={24}
              layouts={layouts.asMutable()}
              onLayoutChange={onLayoutChange}
              margin={[theme.space[1], theme.space[1]]}
              containerPadding={[theme.space[1], theme.space[1]]}
              draggableHandle=".dragMe"
            >
              {panels
                .filter(p => !p.detached)
                .filter(p => p.visible)
                .map(p => this.panelsRenderers[p.key]())}
            </ResponsiveReactGridLayout>
            {panels
              .filter(p => p.detached)
              .filter(p => p.visible)
              .map(p => (
                <Rnd
                  key={p.key}
                  bounds="parent"
                  style={{
                    border: "1px solid " + theme.colors.canvas,
                    boxShadow: "0 0 16px rgba(0, 0, 0, 0.4)"
                  }}
                  dragHandleClassName="dragMe"
                  position={{ x: p.x ? p.x : 100, y: p.y ? p.y : 100 }}
                  size={{ width: p.w ? p.w : 400, height: p.h ? p.h : 400 }}
                  onDragStop={(e, d) => onMovePanel(p.key, d)}
                  onResizeStop={(e, direction, ref, delta, position) => {
                    onResizePanel(p.key, {
                      width: ref.offsetWidth,
                      height: ref.offsetHeight,
                      ...position
                    })
                  }}
                >
                  {this.panelsRenderers[p.key]()}
                </Rnd>
              ))}
          </div>
        </>
      )
    }
  }
}
