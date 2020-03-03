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
import React from "react"
import { Ticker, Balance, OrderBook, Trade, UserTrade, Order } from "./Types"
import { Map } from "immutable"

export interface SocketApi {
  connected: boolean
  tickers: Map<String, Ticker>
  balances: Map<String, Balance>
  trades: Array<Trade>
  userTrades: Array<UserTrade>
  openOrders: Array<Order>
  orderBook: OrderBook
  selectedCoinTicker: Ticker
  createdOrder(Order: Order, timestamp: number): void
  pendingCancelOrder(id: string, timestamp: number): void
  createPlaceholder(Order: Order): void
  removePlaceholder(): void
}

export const SocketContext = React.createContext<SocketApi>(null)
