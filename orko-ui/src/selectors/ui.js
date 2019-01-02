/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
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
