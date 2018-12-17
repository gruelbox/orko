import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  alertsCoin: null,
  referencePriceCoin: null,
  showScripts: false
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
    case types.OPEN_SCRIPTS:
      return Immutable.merge(state, { showScripts: true })
    case types.CLOSE_SCRIPTS:
      return Immutable.merge(state, { showScripts: false })
    default:
      return state
  }
}
