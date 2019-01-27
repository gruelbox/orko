/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  balance: undefined,
  ticker: undefined,
  orders: undefined,
  orderBook: undefined,
  userTradeHistory: undefined,
  trades: undefined,
  lastOrderUpdate: undefined
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
      if (!state.userTradeHistory) {
        return Immutable.merge(state, {
          userTradeHistory: Immutable([action.payload])
        })
      }
      if (
        !!action.payload.id &&
        state.userTradeHistory.some(t => t.id === action.payload.id)
      )
        return state
      return Immutable.merge(state, {
        userTradeHistory: Immutable(
          [action.payload].concat(state.userTradeHistory.slice(0, 48))
        )
      })
    case types.CLEAR_BALANCES:
      return Immutable.merge(state, {
        balance: null
      })
    case types.SET_ORDERS:
      return setOrders(
        state,
        action.payload.orders,
        new Date(action.payload.timestamp)
      )
    case types.SET_ORDERBOOK:
      return Immutable.merge(state, {
        orderBook: action.payload
      })
    case types.SET_USER_TRADES:
      return Immutable.merge(state, {
        userTradeHistory: action.payload
      })
    case types.CANCEL_ORDER:
      console.log("Order cancelled", action.payload)
      return cancelOrder(
        state,
        action.payload.orderId,
        new Date(action.payload.timestamp)
      )
    case types.ADD_ORDER:
      console.log("Added order", action.payload)
      return addOrder(
        state,
        action.payload.order,
        new Date(action.payload.timestamp)
      )
    case types.ORDER_ADDED:
      console.log("Order added", action.payload)
      return orderAdded(
        state,
        action.payload.detail,
        new Date(action.payload.timestamp)
      )
    case types.ORDER_REMOVED:
      console.log("Order removed", action.payload)
      return removeOrder(
        state,
        action.payload.orderId,
        new Date(action.payload.timestamp)
      )
    default:
      return state
  }

  function isOrderUpdateLate(state, timestamp) {
    return (
      state.lastOrderUpdate &&
      timestamp.getTime() < state.lastOrderUpdate.getTime()
    )
  }

  function setOrders(state, orders, timestamp) {
    if (isOrderUpdateLate(state, timestamp)) {
      console.log("Ignoring orders. Late")
      return state
    }
    return Immutable.merge(state, { orders, lastOrderUpdate: timestamp })
  }

  function cancelOrder(state, orderId, timestamp) {
    if (isOrderUpdateLate(state, timestamp)) {
      console.log("Ignoring cancel order. Late")
      return state
    }
    if (!state.orders) return state
    const index = state.orders.findIndex(o => o.id === orderId)
    if (index === -1) return state
    const orders = Immutable.asMutable(state.orders, { deep: true })
    orders[index].status = "CANCELED"
    return Immutable.merge(state, {
      orders,
      lastOrderUpdate: timestamp
    })
  }

  function removeOrder(state, orderId, timestamp) {
    if (isOrderUpdateLate(state, timestamp)) {
      console.log("Ignoring remove order. Late")
      return state
    }
    if (!state.orders) return state
    return Immutable.merge(state, {
      orders: state.orders.filter(o => o.id !== orderId),
      lastOrderUpdate: timestamp
    })
  }

  function addOrder(state, order, timestamp) {
    // When adding an order, assume that any state for it already in the store is more up to date,
    // so use that in preference
    if (!state.orders) return Immutable([order])
    const index = state.orders.findIndex(o => o.id === order.id)
    if (index === -1) {
      return Immutable.merge(state, {
        orders: state.orders.concat(order)
      })
    }
    const orders = Immutable.asMutable(state.orders)
    orders[index] = Immutable.merge(order, orders[index])
    return Immutable.merge(state, { orders, lastOrderUpdate: timestamp })
  }

  function orderAdded(state, order, timestamp) {
    if (isOrderUpdateLate(state, timestamp)) {
      console.log("Ignoring order added. Late")
      return state
    }
    // When reporting an order added, overwrite any existing state for it
    if (!state.orders) return Immutable([order])
    const index = state.orders.findIndex(o => o.id === order.id)
    if (index === -1) {
      return Immutable.merge(state, {
        orders: state.orders.concat(order),
        lastOrderUpdate: timestamp
      })
    }
    const orders = Immutable.asMutable(state.orders)
    orders[index] = Immutable.merge(orders[index], order)
    return Immutable.merge(state, { orders, lastOrderUpdate: timestamp })
  }
}
