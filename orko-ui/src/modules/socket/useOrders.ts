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
import { Order, OrderStatus } from "modules/socket"
import { useReducer, useMemo } from "react"

export interface UseOrderArrayApi {
  clear(): void
  updateSnapshot(orders: Array<Order>, timestamp: number): void
  orderUpdated(order: Order, timestamp: number): void
  pendingCancelOrder(id: string, timestamp: number): void
  createPlaceholder(order: Order): void
  removePlaceholder(): void
}

const PLACEHOLDER_ID = "PLACEHOLDER"

interface BaseAction {
  reduce(state: Array<Order>): Array<Order>
}

class RemovePlaceholderAction implements BaseAction {
  reduce(state: Array<Order>): Array<Order> {
    return state ? state.filter(o => o.id !== PLACEHOLDER_ID) : state
  }
}

class ClearAction implements BaseAction {
  reduce(state: Array<Order>): Array<Order> {
    return Immutable([])
  }
}

class FullUpdateAction implements BaseAction {
  private timestamp: number
  private order: Order

  constructor(order: Order, timestamp: number) {
    this.order = order
    this.timestamp = timestamp
  }

  reduce(state: Array<Order>): Array<Order> {
    return orderUpdated(
      state ? state.filter(o => o.id !== PLACEHOLDER_ID) : state,
      this.order,
      this.timestamp
    )
  }
}

class CreatePlaceholderAction extends FullUpdateAction {
  constructor(order: Order) {
    super({ ...order, id: PLACEHOLDER_ID, status: OrderStatus.PENDING_NEW }, new Date().getTime())
  }
}

class StateUpdateAction implements BaseAction {
  private timestamp: number
  private id: string
  private status: OrderStatus

  constructor(id: string, status: OrderStatus, timestamp: number) {
    this.id = id
    this.status = status
    this.timestamp = timestamp
  }

  reduce(state: Array<Order>): Array<Order> {
    return orderUpdated(
      state ? state.filter(o => o.id !== PLACEHOLDER_ID) : state,
      { id: this.id, status: this.status },
      this.timestamp
    )
  }
}

class UpdateSnapshotAction implements BaseAction {
  private orders: Array<Order>
  private timestamp: number

  constructor(orders: Array<Order>, timestamp: number) {
    this.orders = orders
    this.timestamp = timestamp
  }

  reduce(state: Array<Order>): Array<Order> {
    // Updates for every order mentioned
    let result = state ? state : []
    const idsPresent = new Set<string>()
    for (const order of this.orders) {
      idsPresent.add(order.id)
      result = orderUpdated(result, order, this.timestamp)
    }

    // Any order not mentioned should be removed
    if (state) {
      for (const order of state) {
        if (order.id === PLACEHOLDER_ID || idsPresent.has(order.id)) {
          continue
        }
        result = orderUpdated(
          state,
          {
            id: order.id,
            status:
              order.status === OrderStatus.PENDING_CANCEL ? OrderStatus.CANCELED : OrderStatus.PENDING_CANCEL
          },
          this.timestamp
        )
      }
    }

    return Immutable(result)
  }
}

function reducer(state: Array<Order>, action: BaseAction) {
  return action.reduce(state)
}

export function useOrders(): [Array<Order>, UseOrderArrayApi] {
  const [value, dispatch] = useReducer(reducer, null)
  const api: UseOrderArrayApi = useMemo(
    () => ({
      updateSnapshot: (orders: Array<Order>, timestamp: number) => {
        dispatch(new UpdateSnapshotAction(orders, timestamp))
      },
      orderUpdated: (order: Order, timestamp: number) => {
        dispatch(new FullUpdateAction(order, timestamp !== undefined ? timestamp : order.timestamp))
      },
      pendingCancelOrder: (id: string, timestamp: number) => {
        dispatch(new StateUpdateAction(id, OrderStatus.PENDING_CANCEL, timestamp))
      },
      createPlaceholder: (order: Order) => {
        dispatch(new CreatePlaceholderAction(order))
      },
      removePlaceholder: () => {
        dispatch(new RemovePlaceholderAction())
      },
      clear: () => {
        dispatch(new ClearAction())
      }
    }),
    [dispatch]
  )
  return [value, api]
}

function orderUpdated(state: Array<Order>, order: any, timestamp: number) {
  if (order === null) {
    return Immutable([])
  }

  const isRemoval =
    order.status === OrderStatus.EXPIRED ||
    order.status === OrderStatus.CANCELED ||
    order.status === OrderStatus.FILLED

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
