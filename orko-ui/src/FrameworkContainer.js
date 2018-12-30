import React from "react"
import { connect } from "react-redux"
import Immutable from "seamless-immutable"
import * as uiActions from "./store/ui/actions"
import { getFromLS, saveToLS } from "./util/localStorage"
import Framework from "./Framework"

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

class FrameworkContainer extends React.Component {
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
    this.setState({ layouts: Immutable.merge(baseLayouts, layouts) }, () =>
      saveToLS("layouts", this.state.layouts)
    )
  }

  onAddedLayout = key => {
    const match = it => it.i === key
    const newLayouts = {
      lg: baseLayouts.lg.find(match),
      md: baseLayouts.md.find(match),
      sm: baseLayouts.sm.find(match)
    }
    this.setState(
      state => {
        var lg = state.layouts.lg.asMutable()
        var md = state.layouts.md.asMutable()
        var sm = state.layouts.sm.asMutable()
        if (!lg.find(match)) {
          lg.push(newLayouts.lg)
        }
        if (!md.find(match)) {
          md.push(newLayouts.md)
        }
        if (!sm.find(match)) {
          sm.push(newLayouts.sm)
        }
        return {
          layouts: Immutable({
            lg,
            md,
            sm
          })
        }
      },
      () => saveToLS("layouts", this.state.layouts)
    )
  }

  onChangePanels = panels => {
    this.props.dispatch(uiActions.changePanels(panels))
    panels
      .filter(panel => panel.visible)
      .forEach(panel => this.onAddedLayout(panel.key))
  }

  onToggleViewSettings = () => {
    this.setState(state => ({ showSettings: !state.showSettings }))
  }

  render() {
    return (
      <Framework
        isMobile={this.state.isMobile}
        showSettings={this.state.showSettings}
        panels={this.props.panels}
        layouts={this.state.layouts}
        onToggleViewSettings={this.onToggleViewSettings}
        onHidePanel={this.onHidePanel}
        onChangePanels={this.onChangePanels}
        onResetLayout={this.onResetLayout}
        onLayoutChange={this.onLayoutChange}
      />
    )
  }
}

export default connect(state => ({
  panels: state.ui.panels
}))(FrameworkContainer)
