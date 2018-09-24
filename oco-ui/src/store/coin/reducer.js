import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  balance: undefined,
  ticker: undefined,
  orders: undefined,
  orderBook: undefined,
  userTradeHistory: undefined,
  trades: Immutable([])
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
        trades: Immutable([])
      })
    case types.ADD_TRADE:
      return Immutable.merge(state, {
        trades: Immutable([action.payload].concat(state.trades.slice(0, 48)))
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
    case types.SET_USER_TRADE_HISTORY:
      return Immutable.merge(state, {
        userTradeHistory: action.payload
      })
    case types.CANCEL_ORDER:
      return Immutable.merge(
        state,
        {
          orders: {
            allOpenOrders: state.orders.allOpenOrders.filter(
              o => o.id !== action.payload
            )
          }
        },
        { deep: true }
      )
    default:
      return state
  }
}
