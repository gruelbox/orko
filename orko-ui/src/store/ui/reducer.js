import Immutable from "seamless-immutable"
import * as types from "./actionTypes"
import { getFromLS, saveToLS } from "../../util/localStorage"

const basePanels = Immutable([
  { key: "coins", title: "Coins", visible: true },
  { key: "jobs", title: "Jobs", visible: true },
  { key: "chart", title: "Chart", visible: true },
  { key: "openOrders", title: "Orders", visible: true },
  { key: "balance", title: "Balance", visible: true },
  { key: "tradeSelector", title: "Trading", visible: true },
  { key: "marketData", title: "Market", visible: true },
  { key: "notifications", title: "Notifications", visible: true }
])

const loadedPanels = getFromLS("panels")

const initialState = Immutable({
  alertsCoin: null,
  referencePriceCoin: null,
  panels: loadedPanels === null ? basePanels : loadedPanels
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.OPEN_ALERTS:
      return Immutable.merge(state, { alertsCoin: action.payload })
    case types.CLOSE_ALERTS:
      return Immutable.merge(state, { alertsCoin: null })
    case types.OPEN_REFERENCE_PRICE:
      return Immutable.merge(state, { referencePriceCoin: action.payload })
    case types.CLOSE_REFERENCE_PRICE:
      return Immutable.merge(state, { referencePriceCoin: null })
    case types.CHANGE_PANELS:
      const panels = action.payload
      const reducer = toReduce =>
        toReduce.reduce(function(accumulator, panel) {
          accumulator[panel.key] = panel
          return accumulator
        }, {})
      var current = reducer(state.panels)
      var changes = reducer(panels)
      const updated = Immutable(
        Object.values(Immutable.merge(current, changes, { deep: true }))
      )
      saveToLS("panels", updated)
      return Immutable.merge(state, { panels: updated })
    case types.RESET_PANELS:
      saveToLS("panels", basePanels)
      return Immutable.merge(state, { panels: basePanels })
    default:
      return state
  }
}
