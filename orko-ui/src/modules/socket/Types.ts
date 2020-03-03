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
import { PartialServerCoin, augmentCoin, Coin } from "modules/market"

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

export enum OrderType {
  BID = "BID",
  ASK = "ASK",
  EXIT_ASK = "EXIT_ASK",
  EXIT_BID = "EXIT_BID"
}

export enum OrderStatus {
  PENDING_NEW = "PENDING_NEW",
  NEW = "NEW",
  PARTIALLY_FILLED = "PARTIALLY_FILLED",
  FILLED = "FILLED",
  PENDING_CANCEL = "PENDING_CANCEL",
  PARTIALLY_CANCELED = "PARTIALLY_CANCELED",
  CANCELED = "CANCELED",
  PENDING_REPLACE = "PENDING_REPLACE",
  REPLACED = "REPLACED",
  STOPPED = "STOPPED",
  REJECTED = "REJECTED",
  EXPIRED = "EXPIRED",
  UNKNOWN = "UNKNOWN"
}

export enum RunningAtType {
  SERVER = "SERVER",
  EXCHANGE = "EXCHANGE"
}

export interface BaseOrder {
  type: OrderType
  originalAmount: number
  remainingAmount: number
  stopPrice: number
  limitPrice: number
}

export interface Order extends BaseOrder {
  id: string
  timestamp: number
  status: OrderStatus
  currencyPair: PartialServerCoin
  cumulativeAmount: number
  averagePrice: number
  fee: number
  deleted: boolean
  serverTimestamp: number
}

export interface DisplayOrder extends BaseOrder {
  runningAt: RunningAtType
  jobId: string
}

export interface ExpandedOrder extends Order, DisplayOrder {}

export interface JobOrder {
  type: OrderType
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
