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
import { Order, OrderStatus } from "@orko-ui-socket/index"
import { useReducer, useMemo } from "react"

export interface UseOrderArrayApi {
  orderUpdated(order: Order, timestamp: number): void
  cancelledOrder(id: string, timestamp: number): void
  pendingCancelOrder(id: string, timestamp: number): void
  clearIfTimestampedBefore(timestamp: number): void
  createPlaceholder(order: Order): void
  removePlaceholder(): void
}

type ActionType = "ORDER_UPDATED" | "CLEAR_ORDERS" | "CREATE_PLACEHOLDER" | "REMOVE_PLACEHOLDER"

const PLACEHOLDER_ID = "PLACEHOLDER"

type Action = {
  type: ActionType
  order?: Order
  id?: string
  timestamp?: number
  status?: OrderStatus
}

function reducer(state: Array<Order>, action: Action) {
  switch (action.type) {
    case "CLEAR_ORDERS":
      return state
        ? state.filter(o => o.id === PLACEHOLDER_ID || o.timestamp > action.timestamp)
        : Immutable([])
    case "ORDER_UPDATED":
      return orderUpdated(
        state.filter(o => o.id !== PLACEHOLDER_ID),
        action.order ? action.order : { id: action.id, status: action.status },
        action.timestamp
      )
    case "CREATE_PLACEHOLDER":
      return orderUpdated(
        state.filter(o => o.id !== PLACEHOLDER_ID),
        { ...action.order, id: PLACEHOLDER_ID, status: "PENDING_NEW" },
        action.timestamp
      )
    case "REMOVE_PLACEHOLDER":
      return state.filter(o => o.id !== PLACEHOLDER_ID)
    default:
      return state
  }
}

export function useOrders(): [Array<Order>, UseOrderArrayApi] {
  const [value, dispatch] = useReducer(reducer, null)
  const api: UseOrderArrayApi = useMemo(
    () => ({
      orderUpdated: (order: Order, timestamp: number) =>
        dispatch({
          type: "ORDER_UPDATED",
          order,
          timestamp: timestamp !== undefined ? timestamp : order.timestamp
        }),
      cancelledOrder: (id: string, timestamp: number) =>
        dispatch({ type: "ORDER_UPDATED", id, timestamp, status: "CANCELED" }),
      pendingCancelOrder: (id: string, timestamp: number) =>
        dispatch({ type: "ORDER_UPDATED", id, timestamp, status: "PENDING_CANCEL" }),
      clearIfTimestampedBefore: (timestamp: number) => dispatch({ type: "CLEAR_ORDERS", timestamp }),
      createPlaceholder: (order: Order) =>
        dispatch({
          type: "CREATE_PLACEHOLDER",
          order,
          timestamp: new Date().getTime()
        }),
      removePlaceholder: () =>
        dispatch({
          type: "REMOVE_PLACEHOLDER"
        })
    }),
    [dispatch]
  )
  return [value, api]
}

function orderUpdated(state: Array<Order>, order: any, timestamp: number) {
  if (order === null) {
    console.log(" - Clearing down")
    return Immutable([])
  }

  const isRemoval = order.status === "EXPIRED" || order.status === "CANCELED" || order.status === "FILLED"

  // No orders at all yet
  if (!state) {
    if (isRemoval) return state
    return Immutable([
      {
        ...order,
        deleted: false,
        serverTimestamp: timestamp
      }
    ])
  }

  // This order never seen before
  const index = state.findIndex(o => o.id === order.id)
  if (index === -1) {
    if (isRemoval) return state
    return state.concat({
      ...order,
      deleted: false,
      serverTimestamp: timestamp
    })
  }

  // If we've previously registered the order as removed, then assume
  // this update is late and stop
  const prevVersion = state[index]
  if (prevVersion.deleted) {
    return state
  }

  // If it's a removal, remove
  if (isRemoval) {
    return replaceOrderContent(state, index, { deleted: true })
  }

  // If the previous version is derived from a later timestamp than
  // this update, stop
  if (prevVersion.serverTimestamp > timestamp) {
    return state
  }

  // Overwrite existing state with any values provided in the
  // update
  return replaceOrderContent(state, index, { ...order, serverTimestamp: timestamp })
}

function replaceOrderContent(state: Array<Order>, index: number, replacement: object) {
  const orders = Immutable.asMutable(state, { deep: true })
  const existing = orders[index]
  // eslint-disable-next-line
  for (const key of Object.keys(replacement)) {
    const val = replacement[key]
    if (val !== undefined && val !== null) {
      existing[key] = val
    }
  }
  return Immutable(orders)
}
