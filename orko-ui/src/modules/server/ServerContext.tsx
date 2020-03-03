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
