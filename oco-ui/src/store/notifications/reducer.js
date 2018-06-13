import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  notifications: Immutable([])
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.CLEAR:
      return Immutable.merge(state, { notifications: [] })
    case types.ADD:
      const notification = Immutable.set(Immutable(action.payload), "dateTime", new Date())
      return Immutable.merge(state, { notifications: Immutable([notification]).concat(state.notifications) })
    default:
      return state
  }
}