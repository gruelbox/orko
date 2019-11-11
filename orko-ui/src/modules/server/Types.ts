import { ServerCoin } from "modules/market"

export interface CoinMetadata {
  maximumAmount: number
  minimumAmount: number
  priceScale: number
}

export enum JobType {
  LIMIT_ORDER = "LimitOrderJob",
  SOFT_TRAILING_STOP = "SoftTrailingStop",
  ALERT = "Alert",
  STATUS_UPDATE = "StatusUpdateJob",
  OCO = "OneCancelsOther",
  SCRIPT = "ScriptJob"
}

export enum TradeDirection {
  SELL = "SELL",
  BUY = "BUY"
}

export interface Job {
  id: string
  jobType: JobType
}

export interface ScriptJob extends Job {
  name: string
  script: string
}

export interface OcoThresholdTask {
  thresholdAsString: string
  job: Job
}

export interface OcoJob extends Job {
  tickTrigger: ServerCoin
  high: OcoThresholdTask
  low: OcoThresholdTask
  verbose: boolean
}

export interface LimitOrderJob extends Job {
  tickTrigger: ServerCoin
  direction: TradeDirection
  amount: number
  limitPrice: number
}

export interface SoftTrailingStopJob extends Job {
  tickTrigger: ServerCoin
  direction: TradeDirection
  amount: number
  stopPrice: number
  limitPrice: number
  lastSyncPrice: number
}

export enum AlertLevel {
  ALERT
}

export interface Notification {
  message: string
  level: AlertLevel
}

export interface AlertJob extends Job {
  notification: Notification
}

export interface StatusUpdateJob extends Job {}
