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
import React, { useState, useContext, useEffect } from "react"
import { connect } from "react-redux"
import * as uiActions from "./store/ui/actions"
import { getAllPanels, getAllLayouts } from "./selectors/ui"
import Framework from "./Framework"
import theme from "./theme"
import { AuthContext, AuthApi } from "@orko-ui-auth/Authoriser"
import { DraggableData } from "react-rnd"

const windowToBreakpoint = (width: number) =>
  width < theme.lg ? (width < theme.md ? "sm" : "md") : "lg"

const FrameworkContainer: React.FC<any> = props => {
  const bp = windowToBreakpoint(window.innerWidth)
  const [mobile, setMobile] = useState(bp === "sm")
  const [breakpoint, setBreakpoint] = useState(bp)
  const [width, setWidth] = useState(window.innerWidth)
  const [showSettings, setShowSettings] = useState(false)
  const auth: AuthApi = useContext(AuthContext)

  useEffect(() => {
    window.addEventListener("resize", (e: UIEvent) =>
      setWidth(window.innerWidth)
    )
  })

  const onResetLayout = () => {
    props.dispatch(uiActions.resetPanels())
    props.dispatch(uiActions.resetLayouts())
  }

  const onTogglePanelVisible = (id: string) => {
    props.dispatch(uiActions.togglePanelVisible(id))
  }

  const onTogglePanelAttached = (id: string) => {
    props.dispatch(uiActions.togglePanelAttached(id))
  }

  const onMovePanel = (key: string, d: DraggableData) => {
    props.dispatch(uiActions.movePanel(key, d))
  }

  const onResizePanel = (key: string, d: DraggableData) => {
    props.dispatch(uiActions.resizePanel(key, d))
  }

  const onInteractPanel = (key: string, d: DraggableData) => {
    props.dispatch(uiActions.interactPanel(key))
  }

  const onLayoutChange = (layout, layouts) => {
    props.dispatch(uiActions.updateLayouts(layouts))
  }

  const onToggleViewSettings = () => {
    setShowSettings(!showSettings)
  }

  const onBreakpointChange = (breakpoint: string) => {
    setBreakpoint(breakpoint)
    setMobile(breakpoint === "sm")
  }

  return (
    <Framework
      isMobile={mobile}
      width={width}
      showSettings={showSettings}
      panels={props.panels}
      layouts={props.layouts}
      layoutsAsObj={props.layoutsAsObj[breakpoint]}
      onToggleViewSettings={onToggleViewSettings}
      onTogglePanelAttached={onTogglePanelAttached}
      onTogglePanelVisible={onTogglePanelVisible}
      onResetLayout={onResetLayout}
      onLayoutChange={onLayoutChange}
      onMovePanel={onMovePanel}
      onResizePanel={onResizePanel}
      onInteractPanel={onInteractPanel}
      onBreakpointChange={onBreakpointChange}
      onLogout={auth.logout}
      onClearWhitelisting={auth.clearWhitelisting}
    />
  )
}

export default connect(state => ({
  panels: getAllPanels(state),
  layouts: getAllLayouts(state),
  layoutsAsObj: state.ui.layouts
}))(FrameworkContainer)
