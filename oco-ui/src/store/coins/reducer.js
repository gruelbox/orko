import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  coins: Immutable([])
})

export default function reduce(state = initialState, action = {}) {
  var newCoins
  switch (action.type) {
    case types.SET:
      return { coins: action.payload }
    case types.ADD:
      newCoins = state.coins.concat([action.payload])
      return Immutable({ coins: newCoins })
    case types.REMOVE:
      newCoins = state.coins.filter(c => c.key !== action.payload.key)
      return Immutable({ coins: newCoins })
    default:
      return state
  }
}
