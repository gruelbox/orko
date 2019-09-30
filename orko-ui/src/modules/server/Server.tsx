import React, { useContext, useState, useEffect, useMemo, ReactElement } from "react"
import { ServerContext } from "./ServerContext"
import { AuthContext } from "@orko-ui-auth/index"
import { Coin, ServerCoin, coinFromTicker, tickerFromCoin } from "@orko-ui-market/index"
import exchangesService from "@orko-ui-market/exchangesService"
import { LogContext } from "@orko-ui-log/index"
import Immutable from "seamless-immutable"
import { Map } from "immutable"
import { CoinMetadata } from "./Types"

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
  const trace = logApi.trace

  const [subscriptions, setSubscriptions] = useState<Coin[]>(Immutable([]))
  const [coinMetadata, setCoinMetadata] = useState<Map<string, CoinMetadata>>(Map())

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
        .authenticatedRequest(() => exchangesService.addSubscription(tickerFromCoin(coin)))
        .catch((error: Error) => errorPopup("Could not add subscription: " + error.message))
        .then(() => setSubscriptions(current => insertCoin((current as any).asMutable(), coin)))
        .then(() => fetchMetadata(coin))
    },
    [setSubscriptions, errorPopup, authApi, fetchMetadata]
  )

  const removeSubscription = useMemo(
    () => (coin: Coin) => {
      authApi
        .authenticatedRequest(() => exchangesService.removeSubscription(tickerFromCoin(coin)))
        .catch((error: Error) => errorPopup("Could not remove subscription: " + error.message))
        .then(() => setSubscriptions(current => current.filter(c => c.key !== coin.key)))
    },
    [setSubscriptions, errorPopup, authApi]
  )

  const api = useMemo(
    () => ({
      subscriptions,
      addSubscription,
      removeSubscription,
      coinMetadata
    }),
    [subscriptions, addSubscription, removeSubscription, coinMetadata]
  )

  return <ServerContext.Provider value={api}>{props.children}</ServerContext.Provider>
}

export default Server
