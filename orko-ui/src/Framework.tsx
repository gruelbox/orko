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
import React, { ReactElement } from "react"

import { Route } from "react-router-dom"
import { WidthProvider, Responsive, Layouts, Layout } from "react-grid-layout"
import { Rnd, DraggableData } from "react-rnd"
import styled from "styled-components"
import theme from "./theme"

import CoinsContainer from "./containers/CoinsContainer"
import JobContainer from "./containers/JobContainer"
import JobsContainer from "./containers/JobsContainer"
import ToolbarContainer from "./containers/ToolbarContainer"
import AddCoinContainer from "./containers/AddCoinContainer"
import MarketContainer from "./containers/MarketContainer"
import OrdersContainer from "./containers/OrdersContainer"
import TradeContainer from "./containers/TradeContainer"
import BalanceContainer from "./containers/BalanceContainer"
import ManageAlertsContainer from "./containers/ManageAlertsContainer"
import ManageScriptsContainer from "./containers/ManageScriptsContainer"
import SetReferencePriceContainer from "./containers/SetReferencePriceContainer"
import NewReleaseContainer from "./containers/NewReleaseContainer"
import Chart from "./components/Chart"
import Section from "./components/primitives/Section"
import ViewSettings from "./components/ViewSettings"
import ErrorBoundary from "./components/ErrorBoundary"
import { Provider as SectionProvider } from "./components/primitives/Section"
import { Logs } from "modules/log"
import { Panel, KeyedLayouts } from "useUiConfig"

const ResponsiveReactGridLayout = WidthProvider(Responsive)

const LayoutBox = styled.div`
  height: 100%;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.2);
`

const PositioningWrapper = ({ mobile, children }) =>
  mobile ? <div>{children}</div> : <FloatingPositioningWrapper>{children}</FloatingPositioningWrapper>

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

interface FrameworkProps {
  isMobile: boolean
  width: number
  panels: Panel[]
  hiddenPanels: Panel[]
  layouts: Layouts
  layoutsAsObj: KeyedLayouts
  showSettings: boolean
  onToggleViewSettings(): void
  onTogglePanelAttached(key: string): void
  onTogglePanelVisible(key: string): void
  onResetLayout(): void
  onLayoutChange(layout: Layout[], layouts: Layouts): void
  onMovePanel(key: string, d: DraggableData): void
  onResizePanel(key: string, d: DragData): void
  onInteractPanel(key: string): void
  onBreakpointChange(breakpoint: string): void
  onLogout(): void
  onClearWhitelisting(): void
}

export interface OfAllPanels<T> {
  chart: T
  openOrders: T
  balance: T
  tradeSelector: T
  coins: T
  jobs: T
  marketData: T
  notifications: T
}

interface Renderers extends OfAllPanels<() => ReactElement> {}

export interface DragData {
  x: number
  y: number
  w: number
  h: number
}

export default class Framework extends React.Component<FrameworkProps> {
  panelsRenderers: Renderers = null

  constructor(props: FrameworkProps) {
    super(props)

    const icons = this.props.panels.reduce(function(accumulator, panel) {
      accumulator[panel.key] = panel.icon
      return accumulator
    }, {})

    const Panel: React.FC<{ id: string; children: ReactElement }> = ({ id, children }) => (
      <SectionProvider
        value={{
          draggable: true,
          compactDragHandle: this.props.isMobile,
          icon: icons[id],
          onHide: () => this.props.onTogglePanelVisible(id),
          onToggleAttached: this.props.isMobile ? null : () => this.props.onTogglePanelAttached(id)
        }}
      >
        <ErrorBoundary wrapper={({ message, children }) => <Section heading={message}>{children}</Section>}>
          {children}
        </ErrorBoundary>
      </SectionProvider>
    )

    this.panelsRenderers = {
      chart: () => (
        <LayoutBox key="chart" data-grid={this.props.layoutsAsObj.chart}>
          <Panel id="chart">
            <Chart />
          </Panel>
        </LayoutBox>
      ),
      openOrders: () => (
        <LayoutBox key="openOrders" data-grid={this.props.layoutsAsObj.openOrders}>
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
        <LayoutBox key="tradeSelector" data-grid={this.props.layoutsAsObj.tradeSelector}>
          <Panel id="tradeSelector">
            <TradeContainer />
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
        <LayoutBox key="marketData" data-grid={this.props.layoutsAsObj.marketData}>
          <Panel id="marketData">
            <MarketContainer allowAnimate={!this.props.isMobile} />
          </Panel>
        </LayoutBox>
      ),
      notifications: () => (
        <LayoutBox key="notifications" data-grid={this.props.layoutsAsObj.notifications}>
          <Panel id="notifications">
            <Logs />
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
      hiddenPanels,
      layouts,
      showSettings,
      onToggleViewSettings,
      onTogglePanelVisible,
      onResetLayout,
      onLayoutChange,
      onMovePanel,
      onResizePanel,
      onInteractPanel,
      onBreakpointChange,
      onLogout,
      onClearWhitelisting
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

    const ManageScripts = props => <ManageScriptsContainer {...props} key={props.match.params.id} />

    return (
      <>
        <ErrorBoundary>
          <ToolbarContainer
            mobile={isMobile}
            onShowViewSettings={onToggleViewSettings}
            onTogglePanelVisible={onTogglePanelVisible}
            width={width}
            onLogout={onLogout}
            onClearWhitelist={onClearWhitelisting}
            hiddenPanels={hiddenPanels}
          />
        </ErrorBoundary>
        <ErrorBoundary>
          <NewReleaseContainer />
        </ErrorBoundary>
        <ErrorBoundary>
          <Route exact path="/addCoin" component={AddCoinContainer} />
          <Route exact path="/scripts" component={ManageScripts} />
          <Route exact path="/scripts/:id" component={ManageScripts} />
          <Route path="/job/:jobId" component={JobContainer} />
        </ErrorBoundary>
        <ErrorBoundary>
          <Settings />
        </ErrorBoundary>
        <PositioningWrapper mobile={isMobile}>
          <ErrorBoundary>
            <ManageAlertsContainer mobile={isMobile} />
          </ErrorBoundary>
          <ErrorBoundary>
            <SetReferencePriceContainer mobile={isMobile} />
          </ErrorBoundary>
        </PositioningWrapper>
        <div style={{ padding: "-" + theme.space[1] + "px" }}>
          <ResponsiveReactGridLayout
            breakpoints={theme.panelBreakpoints}
            cols={{ lg: 40, md: 32, sm: 4 }}
            rowHeight={24}
            layouts={(layouts as any).asMutable()}
            onLayoutChange={onLayoutChange}
            onBreakpointChange={onBreakpointChange}
            margin={[theme.space[1], theme.space[1]]}
            containerPadding={[theme.space[1], theme.space[1]]}
            draggableHandle=".dragMe"
          >
            {panels
              .filter(p => !p.detached || isMobile)
              .filter(p => p.visible)
              .map(p => this.panelsRenderers[p.key]())}
          </ResponsiveReactGridLayout>
          {!isMobile &&
            panels
              .filter(p => p.detached)
              .filter(p => p.visible)
              .map(p => (
                <Rnd
                  key={p.key}
                  bounds="parent"
                  style={{
                    border: "1px solid " + theme.colors.canvas,
                    boxShadow: "0 0 16px rgba(0, 0, 0, 0.4)",
                    zIndex: p.stackPosition
                  }}
                  dragHandleClassName="dragMe"
                  position={{ x: p.x ? p.x : 100, y: p.y ? p.y : 100 }}
                  size={{ width: p.w ? p.w : 400, height: p.h ? p.h : 400 }}
                  onDragStart={() => onInteractPanel(p.key)}
                  onResizeStart={() => onInteractPanel(p.key)}
                  onDragStop={(e, d) => onMovePanel(p.key, d)}
                  onResizeStop={(e, direction, ref, delta, position) => {
                    onResizePanel(p.key, {
                      w: ref.offsetWidth,
                      h: ref.offsetHeight,
                      x: position.x,
                      y: position.y
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
