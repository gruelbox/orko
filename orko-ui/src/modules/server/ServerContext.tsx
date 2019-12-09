import React from "react"
import { Coin } from "modules/market"
import { CoinMetadata, Job, ScriptJob } from "./Types"
import { Map } from "immutable"

export interface ServerApi {
  subscriptions: Coin[]
  coinMetadata: Map<string, CoinMetadata>
  jobs: Job[]
  jobsLoading: boolean
  addSubscription(coin: Coin): void
  removeSubscription(coin: Coin): void
  submitJob(job: Job): void
  submitScriptJob(job: ScriptJob): void
  deleteJob(id: string): void
}

export const ServerContext = React.createContext<ServerApi>(null)
