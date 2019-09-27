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
  resubscribe(): void
  createdOrder(Order: Order, timestamp: number): void
  pendingCancelOrder(id: string, timestamp: number): void
  createPlaceholder(Order: Order): void
  removePlaceholder(): void
}

export const SocketContext = React.createContext<SocketApi>(null)
