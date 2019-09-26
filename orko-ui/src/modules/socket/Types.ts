import { PartialServerCoin } from "@orko-ui-market/index"

export interface Ticker {
  bid: number
  last: number
  ask: number
  open: number
  low: number
  high: number
}

export interface Balance {
  total: number
  available: number
}

export interface OrderBook {
  asks: Array<LimitOrder>
  bids: Array<LimitOrder>
  timeStamp: Date
}

export type OrderType = "BID" | "ASK" | "EXIT_ASK" | "EXIT_BID"
export type OrderStatus =
  | "PENDING_NEW"
  | "NEW"
  | "PARTIALLY_FILLED"
  | "FILLED"
  | "PENDING_CANCEL"
  | "PARTIALLY_CANCELED"
  | "CANCELED"
  | "PENDING_REPLACE"
  | "REPLACED"
  | "STOPPED"
  | "REJECTED"
  | "EXPIRED"
  | "UNKNOWN"

export interface LimitOrder {
  type: OrderBook
  originalAmount: number
  currencyPair: PartialServerCoin
  id: string
  timestamp: Date
  status: OrderStatus
  cumulativeAmount: number
  remainingAmount: number
  averagePrice: number
  fee: number
  limitOrder: number
}
