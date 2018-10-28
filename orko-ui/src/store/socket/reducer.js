import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  connected: false
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_CONNECTION_STATE:
      return Immutable.merge(state, { connected: action.payload })
    default:
      return state
  }
}
