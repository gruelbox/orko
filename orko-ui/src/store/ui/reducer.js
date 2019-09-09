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
import * as types from "./actionTypes"
import { getFromLS, saveToLS } from "@orko-js-common/util/localStorage"

const VERSION = 1

const basePanelMetadata = Immutable({
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

const basePanels = Immutable.merge(
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

const baseLayouts = Immutable({
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

const loadedPanels = getFromLS("panels")
const loadedLayouts = getFromLS("layouts")
const meta = getFromLS("layoutMeta")
const version = meta ? (meta.version ? meta.version : 0) : 0

const initialState = Immutable({
  paperTrading: false,
  alertsCoin: null,
  referencePriceCoin: null,
  panels:
    loadedPanels === null || version < VERSION ? basePanels : loadedPanels,
  layouts:
    loadedLayouts === null || version < VERSION ? baseLayouts : loadedLayouts
})

if (version < VERSION) {
  saveToLS("layoutMeta", { version: VERSION })
}

var nextStackPosition =
  Object.values(initialState.panels).reduce(
    (acc, next) => (next.stackPosition > acc ? next.stackPosition : acc),
    0
  ) + 1

export default function reduce(state = initialState, action = {}) {
  const panelsUpdate = (key, partial) =>
    Immutable.merge(state, {
      panels: saveToLS(
        "panels",
        Immutable.merge(
          state.panels,
          {
            [key]: partial
          },
          { deep: true }
        )
      )
    })
  switch (action.type) {
    case types.OPEN_ALERTS:
      return Immutable.merge(state, { alertsCoin: action.payload })
    case types.CLOSE_ALERTS:
      return Immutable.merge(state, { alertsCoin: null })
    case types.OPEN_REFERENCE_PRICE:
      return Immutable.merge(state, { referencePriceCoin: action.payload })
    case types.CLOSE_REFERENCE_PRICE:
      return Immutable.merge(state, { referencePriceCoin: null })
    case types.INTERACT_PANEL:
      return panelsUpdate(action.payload, {
        stackPosition: nextStackPosition++
      })
    case types.TOGGLE_PANEL_ATTACHED:
      return panelsUpdate(action.payload, {
        detached: !state.panels[action.payload].detached,
        stackPosition: nextStackPosition++,
        layouts: saveToLS(
          "layouts",
          Immutable.merge(state.layouts, {
            lg: Immutable.merge(state.layouts.lg, {
              [action.payload]: baseLayouts.lg[action.payload]
            }),
            md: Immutable.merge(state.layouts.lg, {
              [action.payload]: baseLayouts.md[action.payload]
            }),
            sm: Immutable.merge(state.layouts.lg, {
              [action.payload]: baseLayouts.sm[action.payload]
            })
          })
        )
      })
    case types.TOGGLE_PANEL_VISIBLE:
      return panelsUpdate(action.payload, {
        visible: !state.panels[action.payload].visible,
        stackPosition: nextStackPosition++,
        layouts: saveToLS(
          "layouts",
          Immutable.merge(state.layouts, {
            lg: Immutable.merge(state.layouts.lg, {
              [action.payload]: baseLayouts.lg[action.payload]
            }),
            md: Immutable.merge(state.layouts.lg, {
              [action.payload]: baseLayouts.md[action.payload]
            }),
            sm: Immutable.merge(state.layouts.lg, {
              [action.payload]: baseLayouts.sm[action.payload]
            })
          })
        )
      })
    case types.RESET_PANELS:
      return Immutable.merge(state, {
        panels: saveToLS("panels", basePanels)
      })
    case types.RESET_ALL_LAYOUTS:
      return Immutable.merge(state, {
        layouts: saveToLS("layouts", baseLayouts)
      })
    case types.UPDATE_LAYOUTS:
      return Immutable.merge(state, {
        layouts: saveToLS(
          "layouts",
          Immutable.merge(baseLayouts, action.payload, { deep: true })
        )
      })
    case types.MOVE_PANEL:
      return panelsUpdate(action.payload.key, {
        x: action.payload.position.x,
        y: action.payload.position.y
      })
    case types.RESIZE_PANEL:
      return panelsUpdate(action.payload.key, {
        x: action.payload.dimensions.x,
        y: action.payload.dimensions.y,
        w: action.payload.dimensions.width,
        h: action.payload.dimensions.height
      })
    case types.ACCEPT_PAPER_TRADING:
      return Immutable.merge(state, {
        paperTrading: true
      })
    default:
      return state
  }
}
