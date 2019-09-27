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
  ticker: undefined,
  orders: undefined,
  userTradeHistory: undefined
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.ORDER_UPDATED:
      return orderUpdated(state, action.payload.order, action.payload.timestamp)
    case types.CLEAR_ORDERS:
      return Immutable.merge(state, {
        orders: undefined
      })
    default:
      return state
  }

  function orderUpdated(state, order, timestamp) {
    if (order === null) {
      return Immutable.merge(state, {
        orders: []
      })
    }

    const isRemoval = order.status === "EXPIRED" || order.status === "CANCELED" || order.status === "FILLED"

    // No orders at all yet
    if (!state.orders) {
      if (isRemoval) return state
      return Immutable.merge(state, {
        orders: [
          {
            ...order,
            deleted: false,
            serverTimestamp: timestamp
          }
        ]
      })
    }

    // This order never seen before
    const index = state.orders.findIndex(o => o.id === order.id)
    if (index === -1) {
      if (isRemoval) return state
      return Immutable.merge(state, {
        orders: state.orders.concat({
          ...order,
          deleted: false,
          serverTimestamp: timestamp
        })
      })
    }

    // If we've previously registered the order as removed, then assume
    // this update is late and stop
    const prevVersion = state.orders[index]
    if (prevVersion.deleted) return state

    // If it's a removal, remove
    if (isRemoval) return replaceOrderContent(index, { deleted: true })

    // If the previous version is derived from a later timestamp than
    // this update, stop
    if (prevVersion.serverTimestamp > timestamp) return state

    // Overwrite existing state with any values provided in the
    // update
    return replaceOrderContent(index, { ...order, serverTimestamp: timestamp })
  }

  function replaceOrderContent(index, replacement) {
    const orders = Immutable.asMutable(state.orders, { deep: true })
    const existing = orders[index]
    // eslint-disable-next-line
    for (const key of Object.keys(replacement)) {
      const val = replacement[key]
      if (val !== undefined && val !== null) {
        existing[key] = val
      }
    }
    return Immutable.merge(state, { orders })
  }
}
