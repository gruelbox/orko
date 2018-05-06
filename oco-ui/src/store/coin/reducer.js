import Immutable from "seamless-immutable"
import PropTypes from "prop-types"
import * as types from "./actionTypes"

const initialState = Immutable({
  balance: undefined,
  balanceUnavailable: false,
  ticker: undefined,
  orders: undefined,
  ordersUnavailable: false
})

export const coinShape = {
  counter: PropTypes.string.isRequired,
  base: PropTypes.string.isRequired,
  exchange: PropTypes.string.isRequired,
  key: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  shortName: PropTypes.string.isRequired
}

export const balanceShape = {
  available: PropTypes.string.isRequired,
  total: PropTypes.string.isRequired
}

export const tickerShape = {
  bid: PropTypes.number.isRequired,
  ask: PropTypes.number.isRequired,
  last: PropTypes.number.isRequired,
  high: PropTypes.number.isRequired,
  low: PropTypes.number.isRequired,
  open: PropTypes.number.isRequired
}

export function coin(exchange, counter, base) {
  return augmentCoin(
    {
      counter: counter,
      base: base
    },
    exchange
  )
}

export function coinFromKey(key) {
  const split = key.split("/")
  return augmentCoin(
    {
      counter: split[1],
      base: split[2]
    },
    split[0]
  )
}

export function augmentCoin(p, exchange) {
  return Immutable.merge(p, {
    exchange: exchange,
    key: exchange + "/" + p.counter + "/" + p.base,
    name: p.base + "/" + p.counter + " (" + exchange + ")",
    shortName: p.base + "/" + p.counter
  })
}

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_BALANCE:
      console.debug(action.type, action)
      return Immutable.merge(state, { balance: action.balance })
    case types.SET_ORDERS:
      console.debug(action.type, action)
      return Immutable.merge(state, {
        orders: Immutable(action.orders),
        ordersUnavailable: false
      })
    case types.CANCEL_ORDER:
      console.debug(action.type, action)
      return Immutable.merge(
        state,
        {
          orders: {
            allOpenOrders: state.orders.allOpenOrders.filter(
              o => o.id !== action.orderId
            )
          }
        },
        { deep: true }
      )
    case types.SET_ORDERS_UNAVAILABLE:
      console.debug(action.type, action)
      return Immutable.merge(state, {
        ordersUnavailable: true
      })
    case types.SET_BALANCE_UNAVAILABLE:
      console.debug(action.type, action)
      return Immutable.merge(state, {
        balanceUnavailable: true
      })
    default:
      return state
  }
}
