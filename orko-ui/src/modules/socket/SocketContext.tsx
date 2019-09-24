import React from "react"
import { Ticker } from "./Types"
import { Map } from "immutable"

export interface SocketApi {
  connected: boolean
  resubscribe(): void
  tickers: Map<String, Ticker>
  selectedCoinTicker: Ticker
}

export const SocketContext = React.createContext<SocketApi>(null)
