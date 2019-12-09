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
import Immutable from "seamless-immutable"
import { getFromLS, saveToLS } from "modules/common/util/localStorage"
import { Layout } from "react-grid-layout"
import { useReducer, useMemo } from "react"
import { OfAllPanels } from "Framework"

const VERSION = 1

interface BasePanel {
  title: string
  icon: string
}

interface BasePanels {
  [P: string]: BasePanel
}

export interface Panel extends BasePanel {
  key: string
  visible: boolean
  detached: boolean
  stackPosition: number
  x: number
  y: number
  w: number
  h: number
}

export interface AllKeyedPanels extends OfAllPanels<Panel> {}

export interface KeyedLayouts extends OfAllPanels<Layout> {}

export interface UiConfig {
  layouts: AllKeyedLayouts
  panels: AllKeyedPanels
}

interface Meta {
  version: number
}

export interface AllKeyedLayouts {
  lg: KeyedLayouts
  md: KeyedLayouts
  sm: KeyedLayouts
}

const basePanelMetadata: BasePanels = Immutable({
  coins: {
    title: "Coins",
    icon: "bitcoin"
  },
  jobs: {
    title: "Jobs",
    icon: "tasks"
  },
  chart: {
    title: "Chart",
    icon: "chart bar outline"
  },
  openOrders: {
    title: "Orders",
    icon: "shopping cart"
  },
  balance: {
    title: "Balance",
    icon: "balance"
  },
  tradeSelector: {
    title: "Trading",
    icon: "call"
  },
  marketData: {
    title: "Market",
    icon: "book"
  },
  notifications: {
    title: "Notifications",
    icon: "warning circle"
  }
})

const basePanels: AllKeyedPanels = Immutable.merge(
  {
    coins: {
      key: "coins",
      visible: true,
      detached: false,
      stackPosition: 0
    },
    jobs: {
      key: "jobs",
      visible: true,
      detached: false,
      stackPosition: 0
    },
    chart: {
      key: "chart",
      visible: true,
      detached: false,
      stackPosition: 0
    },
    openOrders: {
      key: "openOrders",
      visible: true,
      detached: false,
      stackPosition: 0
    },
    balance: {
      key: "balance",
      visible: true,
      detached: false,
      stackPosition: 0
    },
    tradeSelector: {
      key: "tradeSelector",
      visible: true,
      detached: false,
      stackPosition: 0
    },
    marketData: {
      key: "marketData",
      visible: true,
      detached: false,
      stackPosition: 0
    },
    notifications: {
      key: "notifications",
      visible: true,
      detached: false,
      stackPosition: 0
    }
  },
  basePanelMetadata,
  { deep: true }
)

const baseLayouts: AllKeyedLayouts = Immutable({
  lg: {
    coins: { i: "coins", x: 0, y: 100, w: 8, h: 22 },
    notifications: { i: "notifications", x: 0, y: 200, w: 8, h: 9 },
    chart: { i: "chart", x: 8, y: 100, w: 18, h: 18 },
    balance: { i: "balance", x: 8, y: 200, w: 18, h: 4 },
    tradeSelector: { i: "tradeSelector", x: 8, y: 300, w: 18, h: 9 },
    marketData: { i: "marketData", x: 26, y: 100, w: 14, h: 11 },
    openOrders: { i: "openOrders", x: 26, y: 200, w: 14, h: 11 },
    jobs: { i: "jobs", x: 26, y: 300, w: 14, h: 9 }
  },
  md: {
    chart: { i: "chart", x: 0, y: 100, w: 20, h: 13 },
    openOrders: { i: "openOrders", x: 0, y: 200, w: 20, h: 5 },
    balance: { i: "balance", x: 0, y: 300, w: 20, h: 4 },
    tradeSelector: { i: "tradeSelector", x: 0, y: 400, w: 20, h: 9 },
    jobs: { i: "coins", x: 20, y: 100, w: 12, h: 11 },
    marketData: { i: "marketData", x: 20, y: 200, w: 12, h: 8 },
    coins: { i: "jobs", x: 20, y: 300, w: 12, h: 5 },
    notifications: { i: "notifications", x: 20, y: 400, w: 12, h: 7 }
  },
  sm: {
    coins: { i: "coins", x: 0, y: 100, w: 4, h: 12 },
    chart: { i: "chart", x: 0, y: 200, w: 4, h: 12 },
    openOrders: { i: "openOrders", x: 0, y: 300, w: 4, h: 6 },
    balance: { i: "balance", x: 0, y: 400, w: 4, h: 4 },
    tradeSelector: { i: "tradeSelector", x: 0, y: 500, w: 4, h: 9 },
    marketData: { i: "marketData", x: 0, y: 600, w: 4, h: 6 },
    jobs: { i: "jobs", x: 0, y: 700, w: 4, h: 6 },
    notifications: { i: "notifications", x: 0, y: 800, w: 4, h: 6 }
  }
})

const loadedPanels: AllKeyedPanels = getFromLS("panels")
const loadedLayouts: AllKeyedLayouts = getFromLS("layouts")
const meta: Meta = getFromLS("layoutMeta")
const version = meta ? (meta.version ? meta.version : 0) : 0

if (version < VERSION) {
  saveToLS("layoutMeta", { version: VERSION })
}

const initPanels: AllKeyedPanels = loadedPanels === null || version < VERSION ? basePanels : loadedPanels
const initLayouts: AllKeyedLayouts = loadedLayouts === null || version < VERSION ? baseLayouts : loadedLayouts

var nextStackPosition =
  Object.values(initPanels).reduce((acc, next) => (next.stackPosition > acc ? next.stackPosition : acc), 0) +
  1

////////////////////////////////////////////////////////////////////////////////////////////////////////////

interface BaseAction {
  reduce(state: UiConfig): UiConfig
}

type PanelTransform = (panel: Panel) => object

class PartialPanelUpdate implements BaseAction {
  private key: string
  private panelTransform: PanelTransform
  private resetLayouts: boolean

  constructor(key: string, panelTransform: PanelTransform, resetLayouts: boolean = false) {
    this.key = key
    this.panelTransform = panelTransform
    this.resetLayouts = resetLayouts
  }

  reduce(state: UiConfig): UiConfig {
    return Immutable.merge(state, {
      panels: saveToLS(
        "panels",
        Immutable.merge(
          state.panels,
          {
            [this.key]: this.panelTransform(state.panels[this.key])
          },
          { deep: true }
        )
      ),
      layouts: this.resetLayouts
        ? saveToLS(
            "layouts",
            Immutable.merge(state.layouts, {
              lg: Immutable.merge(state.layouts.lg, {
                [this.key]: baseLayouts.lg[this.key]
              }),
              md: Immutable.merge(state.layouts.lg, {
                [this.key]: baseLayouts.md[this.key]
              }),
              sm: Immutable.merge(state.layouts.lg, {
                [this.key]: baseLayouts.sm[this.key]
              })
            })
          )
        : state.layouts
    })
  }
}

class UpdateLayoutsAction implements BaseAction {
  private payload: any

  constructor(payload: any) {
    this.payload = payload
  }

  reduce(state: UiConfig): UiConfig {
    // The filter for size 1 is to workaround a a bug in react-grid-layout
    // https://github.com/STRML/react-grid-layout/issues/870
    return Immutable.merge(state, {
      layouts: saveToLS(
        "layouts",
        Immutable.merge(
          baseLayouts,
          {
            lg: this.payload.lg
              .filter((l: Layout) => l.w !== 1)
              .reduce((acc, val) => {
                acc[val.i] = val
                return acc
              }, {}),
            md: this.payload.md
              .filter((l: Layout) => l.w !== 1)
              .reduce((acc, val) => {
                acc[val.i] = val
                return acc
              }, {}),
            sm: this.payload.sm
              .filter((l: Layout) => l.w !== 1)
              .reduce((acc, val) => {
                acc[val.i] = val
                return acc
              }, {})
          },
          { deep: true }
        )
      )
    })
  }
}

class ResetPanelsAction implements BaseAction {
  reduce(state: UiConfig): UiConfig {
    return Immutable.merge(state, {
      panels: saveToLS("panels", basePanels)
    })
  }
}

class ResetLayoutsAction implements BaseAction {
  reduce(state: UiConfig): UiConfig {
    return Immutable.merge(state, {
      layouts: saveToLS("layouts", baseLayouts)
    })
  }
}

class ResetPanelsAndLayoutsAction implements BaseAction {
  reduce(state: UiConfig): UiConfig {
    return new ResetLayoutsAction().reduce(new ResetPanelsAction().reduce(state))
  }
}

function reducer(state: UiConfig, action: BaseAction) {
  return action.reduce(state)
}

export interface UiConfigApi {
  panelToFront(key: string): void
  togglePanelAttached(key: string): void
  togglePanelVisible(key: string): void
  movePanel(key: string, x: number, y: number): void
  resizePanel(key: string, x: number, y: number, w: number, h: number): void
  resetPanels(): void
  resetLayouts(): void
  resetPanelsAndLayouts(): void
  updateLayouts(payload: object): void
}

function useUiConfig(): [UiConfig, UiConfigApi] {
  const [uiConfig, dispatch] = useReducer(reducer, { panels: initPanels, layouts: initLayouts })
  const api: UiConfigApi = useMemo(
    () => ({
      panelToFront: (key: string) => {
        dispatch(
          new PartialPanelUpdate(key, () => ({
            stackPosition: nextStackPosition++
          }))
        )
      },
      togglePanelAttached: (key: string) => {
        dispatch(
          new PartialPanelUpdate(
            key,
            (current: Panel) => ({
              detached: !current.detached,
              stackPosition: nextStackPosition++
            }),
            true
          )
        )
      },
      togglePanelVisible: (key: string) => {
        dispatch(
          new PartialPanelUpdate(
            key,
            (current: Panel) => ({
              visible: !current.visible,
              stackPosition: nextStackPosition++
            }),
            true
          )
        )
      },
      movePanel: (key: string, x: number, y: number) => {
        dispatch(new PartialPanelUpdate(key, () => ({ x, y })))
      },
      resizePanel: (key: string, x: number, y: number, w: number, h: number) => {
        dispatch(new PartialPanelUpdate(key, () => ({ x, y, w, h })))
      },
      resetPanels: () => {
        dispatch(new ResetPanelsAction())
      },
      resetLayouts: () => {
        dispatch(new ResetLayoutsAction())
      },
      resetPanelsAndLayouts: () => {
        dispatch(new ResetPanelsAndLayoutsAction())
      },
      updateLayouts: (payload: object) => {
        dispatch(new UpdateLayoutsAction(payload))
      }
    }),
    [dispatch]
  )
  return [uiConfig, api]
}

export default useUiConfig
