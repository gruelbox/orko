import React from "react"
import { Coin } from "@orko-ui-market/index"
import { CoinMetadata } from "./Types"
import { Map } from "immutable"

export interface ServerApi {
  subscriptions: Coin[]
  coinMetadata: Map<string, CoinMetadata>
  addSubscription(coin: Coin): void
  removeSubscription(coin: Coin): void
}

export const ServerContext = React.createContext<ServerApi>(null)
