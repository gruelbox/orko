import React from "react"
import { Ticker, Balance, OrderBook, Trade, UserTrade } from "./Types"
import { Map } from "immutable"

export interface SocketApi {
  connected: boolean
  resubscribe(): void
  tickers: Map<String, Ticker>
  balances: Map<String, Balance>
  trades: Array<Trade>
  userTrades: Array<UserTrade>
  orderBook: OrderBook
  selectedCoinTicker: Ticker
}

export const SocketContext = React.createContext<SocketApi>(null)
