import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const LOCAL_STORAGE_KEY = "CoinContainer.coins"
const loaded = localStorage.getItem(LOCAL_STORAGE_KEY)

const initialState = Immutable({
  coins: loaded ? Immutable(JSON.parse(loaded)) : Immutable([])
})

export default function reduce(state = initialState, action = {}) {
  var newCoins
  var newState
  switch (action.type) {
    case types.ADD:
      console.debug(action.type, action)
      newCoins = state.coins.concat([action.coin])
      newState = Immutable({ coins: newCoins })
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newCoins))
      return newState
    case types.REMOVE:
      console.debug(action.type, action)
      newCoins = state.coins.filter(c => c.key !== action.coin.key)
      newState = Immutable({ coins: newCoins })
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newCoins))
      return newState
    default:
      return state
  }
}
