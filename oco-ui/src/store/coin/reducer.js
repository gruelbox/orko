import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  balance: undefined,
  ticker: undefined,
  orders: undefined,
  orderBook: undefined,
  userTradeHistory: undefined,
  trades: undefined
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_BALANCE:
      return Immutable.merge(
        state,
        {
          balance: {
            [action.payload.currency]: action.payload.balance
          }
        },
        { deep: true }
      )
    case types.CLEAR_TRADES:
      return Immutable.merge(state, {
        trades: null
      })
    case types.ADD_TRADE:
      return Immutable.merge(state, {
        trades: state.trades
          ? Immutable([action.payload].concat(state.trades.slice(0, 48)))
          : Immutable([action.payload])
      })
    case types.ADD_USER_TRADE:
      if (
        !!action.payload.id &&
        state.userTradeHistory.some(t => t.id === action.payload.id)
      )
        return state
      return Immutable.merge(state, {
        userTradeHistory: state.userTradeHistory
          ? Immutable(
              [action.payload].concat(state.userTradeHistory.slice(0, 48))
            )
          : Immutable([action.payload])
      })
    case types.CLEAR_BALANCES:
      return Immutable.merge(state, {
        balance: null
      })
    case types.SET_ORDERS:
      return Immutable.merge(state, {
        orders: action.payload
      })
    case types.SET_ORDERBOOK:
      return Immutable.merge(state, {
        orderBook: action.payload
      })
    case types.SET_USER_TRADES:
      return Immutable.merge(state, {
        userTradeHistory: action.payload
      })
    case types.CANCEL_ORDER:
      const index = state.orders.findIndex(o => o.id === action.payload)
      if (index === -1) return state
      const orders = Immutable.asMutable(state.orders, { deep: true })
      orders[index].status = "CANCELED"
      return Immutable.merge(state, {
        orders
      })
    case types.ADD_ORDER:
      return Immutable.merge(state, {
        orders: state.orders.concat(action.payload)
      })
    default:
      return state
  }
}
