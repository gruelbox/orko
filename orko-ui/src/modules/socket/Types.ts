import { PartialServerCoin, augmentCoin, Coin } from "@orko-ui-market/index"

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

type RunningAtType = "SERVER" | "EXCHANGE"

export interface Order {
  type: OrderType
  originalAmount: number
  currencyPair: PartialServerCoin
  id: string
  timestamp: number
  status: OrderStatus
  cumulativeAmount: number
  remainingAmount: number
  averagePrice: number
  fee: number
  deleted: boolean
  serverTimestamp: number
  runningAt: RunningAtType
  jobId: string
}

export interface LimitOrder extends Order {
  limitPrice: number
}

export interface StopOrder extends Order {
  limitPrice: number
  stopPrice: number
}

export interface ServerTrade {
  t: OrderType
  a: number
  c: PartialServerCoin
  p: number
  d: Date
  id: string
  oid: string
  fa: number
  fc: string
}

export class Trade {
  type: OrderType
  originalAmount: number
  coin: Coin
  price: number
  timestamp: Date
  id: String

  constructor(source: ServerTrade, exchange: string) {
    this.type = source.t
    this.originalAmount = source.a
    this.coin = augmentCoin(source.c, exchange)
    this.price = source.p
    this.timestamp = new Date(source.d)
    this.id = source.id
  }
}

export class UserTrade extends Trade {
  feeAmount: number
  feeCurrency: string

  constructor(source: ServerTrade, exchange: string) {
    super(source, exchange)
    this.feeAmount = source.fa
    this.feeCurrency = source.fc
  }
}
