import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  alertsCoin: null
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.OPEN_ALERTS:
      return Immutable.merge(state, { alertsCoin: action.coin })
    case types.CLOSE_ALERTS:
      return Immutable.merge(state, { alertsCoin: null })
    default:
      return state
  }
}
