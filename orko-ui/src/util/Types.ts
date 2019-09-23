import { bindActionCreators } from "redux"

export interface PartialTicker {
  base: string
  counter: string
}

export interface ServerTicker extends PartialTicker {
  exchange: string
}

export interface Coin extends ServerTicker {
  key: string
  name: string
  shortName: string
}

export interface Ticker {
  bid: number
  last: number
  ask: number
  open: number
  low: number
  high: number
}
