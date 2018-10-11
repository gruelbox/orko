import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  notifications: Immutable([]),
  statusCallbacks: Immutable([])
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.CLEAR:
      return Immutable.merge(state, { notifications: [] })
    case types.ADD:
      const notification = Immutable.set(
        Immutable(action.payload),
        "dateTime",
        new Date()
      )
      return Immutable.merge(state, {
        notifications: Immutable([notification]).concat(state.notifications)
      })
    case types.COMPLETE_CALLBACK:
      //console.log(action.type, action)
      return Immutable.merge(state, {
        statusCallbacks: Immutable.without(state.statusCallbacks, [
          action.payload
        ])
      })
    case types.REQUEST_CALLBACK:
      //console.log(action.type, action)
      return Immutable.merge(
        state,
        {
          statusCallbacks: {
            [action.payload.requestId]: {
              callback: action.payload.callback
            }
          }
        },
        { deep: true }
      )
    case types.DEFER_CALLBACK:
      //console.log(action.type, action)
      return Immutable.merge(
        state,
        {
          statusCallbacks: {
            [action.payload.requestId]: {
              status: action.payload
            }
          }
        },
        { deep: true }
      )
    default:
      return state
  }
}
