import { createSelector } from "reselect"
import Immutable from "seamless-immutable"

const getPanels = state => state.ui.panels
const getLayouts = state => state.ui.layouts

export const getAllPanels = createSelector(
  [getPanels],
  panels => Immutable(Object.values(panels))
)

export const getAllLayouts = createSelector(
  [getLayouts],
  layouts =>
    Immutable({
      lg: Object.values(layouts.lg),
      md: Object.values(layouts.md),
      sm: Object.values(layouts.sm)
    })
)

export const getHiddenPanels = createSelector(
  [getAllPanels],
  panels => (panels ? panels.filter(panel => !panel.visible) : Immutable([]))
)
