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
import React, { useContext, useState, useEffect, useMemo, ReactElement } from "react"
import { ServerContext } from "./ServerContext"
import { AuthContext } from "modules/auth"
import { Coin, ServerCoin, coinFromTicker, tickerFromCoin } from "modules/market"
import exchangesService from "modules/market/exchangesService"
import { LogContext } from "modules/log"
import Immutable from "seamless-immutable"
import { Map } from "immutable"
import { CoinMetadata, Job, ScriptJob } from "./Types"
import { useInterval } from "modules/common/util/hookUtils"
import jobService from "./jobService"
import { AuthenticatedRequestResponseType } from "modules/auth"

interface ServerProps {
  children: ReactElement
}

function compareCoins(a: ServerCoin, b: ServerCoin) {
  if (a.exchange < b.exchange) return -1
  if (a.exchange > b.exchange) return 1
  if (a.base < b.base) return -1
  if (a.base > b.base) return 1
  if (a.counter < b.counter) return -1
  if (a.counter > b.counter) return 1
  return 0
}

function insertCoin(arr: Coin[], coin: Coin) {
  for (var i = 0, len = arr.length; i < len; i++) {
    if (compareCoins(coin, arr[i]) < 0) {
      arr.splice(i, 0, coin)
      return arr
    }
  }
  return arr.concat([coin])
}

const Server: React.FC<ServerProps> = (props: ServerProps) => {
  const authApi = useContext(AuthContext)
  const logApi = useContext(LogContext)
  const errorPopup = logApi.errorPopup
  const errorLog = logApi.localError
  const trace = logApi.trace

  const [subscriptions, setSubscriptions] = useState<Coin[]>(Immutable([]))
  const [coinMetadata, setCoinMetadata] = useState<Map<string, CoinMetadata>>(Map())
  const [jobs, setJobs] = useState<Job[]>(null)

  const fetchMetadata = useMemo(
    () => (coin: Coin) => {
      authApi
        .authenticatedRequest(() => exchangesService.fetchMetadata(coin))
        .catch((error: Error) => errorPopup("Could not fetch coin metadata: " + error.message))
        .then((meta: CoinMetadata) => setCoinMetadata(current => current.set(coin.key, meta)))
        .then(() => trace("Fetched metadata for " + coin.shortName))
    },
    [authApi, setCoinMetadata, errorPopup, trace]
  )

  const fetchJobs = useMemo(
    () => () => {
      authApi
        .authenticatedRequest(() => jobService.fetchJobs())
        .catch((error: Error) => errorLog("Could not fetch jobs: " + error.message))
        .then((jobs: Job[]) => setJobs(Immutable(jobs)))
    },
    [authApi, errorLog]
  )

  const submitJob = useMemo(
    () => (job: Job) => {
      authApi
        .authenticatedRequest(() => jobService.submitJob(job), {
          responseType: AuthenticatedRequestResponseType.NONE
        })
        .catch((error: Error) => errorPopup("Could not submit job: " + error.message))
        .then(() => setJobs(current => (current === null ? Immutable([job]) : current.concat(job))))
    },
    [authApi, errorPopup]
  )

  const submitScriptJob = useMemo(
    () => (job: ScriptJob) => {
      authApi
        .authenticatedRequest(() => jobService.submitScriptJob(job), {
          responseType: AuthenticatedRequestResponseType.NONE
        })
        .catch((error: Error) => errorPopup("Could not submit job: " + error.message))
        .then(() => setJobs(current => (current === null ? Immutable([job]) : current.concat(job))))
    },
    [authApi, errorPopup]
  )

  const deleteJob = useMemo(
    () => (id: string) => {
      authApi
        .authenticatedRequest(() => jobService.deleteJob(id), {
          responseType: AuthenticatedRequestResponseType.NONE
        })
        .catch((error: Error) => errorPopup("Could not delete job: " + error.message))
        .then(() => setJobs(current => (current === null ? null : current.filter(j => j.id !== id))))
    },
    [authApi, errorPopup]
  )

  useEffect(() => {
    authApi
      .authenticatedRequest(() => exchangesService.fetchSubscriptions())
      .catch((error: Error) => errorPopup("Could not fetch coin list: " + error.message))
      .then((data: ServerCoin[]) => {
        const coins = data.map((t: ServerCoin) => coinFromTicker(t))
        setSubscriptions(Immutable(coins.sort(compareCoins)))
        trace("Fetched " + data.length + " subscriptions")
        coins.forEach(coin => fetchMetadata(coin))
      })
  }, [setSubscriptions, authApi, fetchMetadata, errorPopup, trace])

  const addSubscription = useMemo(
    () => (coin: Coin) => {
      authApi
        .authenticatedRequest(() => exchangesService.addSubscription(tickerFromCoin(coin)), {
          responseType: AuthenticatedRequestResponseType.NONE
        })
        .catch((error: Error) => errorPopup("Could not add subscription: " + error.message))
        .then(() => setSubscriptions(current => Immutable(insertCoin((current as any).asMutable(), coin))))
        .then(() => fetchMetadata(coin))
    },
    [setSubscriptions, errorPopup, authApi, fetchMetadata]
  )

  const removeSubscription = useMemo(
    () => (coin: Coin) => {
      authApi
        .authenticatedRequest(() => exchangesService.removeSubscription(tickerFromCoin(coin)), {
          responseType: AuthenticatedRequestResponseType.NONE
        })
        .catch((error: Error) => errorPopup("Could not remove subscription: " + error.message))
        .then(() => setSubscriptions(current => current.filter(c => c.key !== coin.key)))
    },
    [setSubscriptions, errorPopup, authApi]
  )

  // Fetch jobs every 5 seconds as this doesn't come down the websocket
  useInterval(
    () => {
      fetchJobs()
    },
    authApi.authorised ? 5000 : null,
    [fetchJobs, authApi]
  )

  const api = useMemo(
    () => ({
      subscriptions,
      addSubscription,
      removeSubscription,
      coinMetadata,
      jobs: jobs ? jobs : Immutable([]),
      jobsLoading: !jobs,
      submitJob,
      submitScriptJob,
      deleteJob
    }),
    [
      subscriptions,
      addSubscription,
      removeSubscription,
      coinMetadata,
      jobs,
      submitJob,
      submitScriptJob,
      deleteJob
    ]
  )

  return <ServerContext.Provider value={api}>{props.children}</ServerContext.Provider>
}

export default Server
