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
import React, { useEffect, ReactElement, useContext, useState, useMemo, useCallback, useRef } from "react"
import ReactDOM from "react-dom";

import { useInterval } from "modules/common/util/hookUtils"

import { AuthContext } from "modules/auth"
import { LogContext, LogRequest } from "modules/log"

import * as socketClient from "./socket.client"
import { locationToCoin } from "../../selectors/coins"
import { SocketContext, SocketApi } from "./SocketContext"
import { Coin } from "modules/market"
import { Map } from "immutable"
import { Ticker, Balance, OrderBook, Trade, UserTrade, Order } from "./Types"
import { useArray } from "modules/common/util/hookUtils"
import { useOrders } from "./useOrders"
import { ServerContext } from "modules/server"

const MAX_PUBLIC_TRADES = 48
const UPDATE_FREQUENCY = 1000

export interface SocketProps {
  getLocation()
  children: ReactElement
}

enum BatchScope {
  GLOBAL,
  COIN
}

class BatchItem {
  scope: BatchScope
  process: () => void
  constructor(scope: BatchScope, process: () => void) {
    this.scope = scope;
    this.process = process;
  }
}

/**
 * Manages the socket, disconnecting when authentication is lost and
 * reconnecting when enabled, and then dispatching any updates to
 * the store.
 *
 * This is an interim measure as I break up the redux store and switch
 * to individual contexts, as has now been done for this and Authoriser.
 *
 * @param props
 */
export const Socket: React.FC<SocketProps> = (props: SocketProps) => {
  ////////////////////// BATCHED REFRESHES /////////////////////////
  const batch = useRef(new Array<BatchItem>())
  useInterval(
    () => {
      ReactDOM.unstable_batchedUpdates(() => {
        if (batch.current.length > 1) {
          batch.current.forEach(it => it.process())
          batch.current = new Array<BatchItem>()
        }
      })
    },
    UPDATE_FREQUENCY,
    [batch]
  )
  const clearBatchItemsForCoin = () => {
    batch.current = batch.current.filter(it => it.scope !== BatchScope.COIN);
  }
  const addToBatch = (scope: BatchScope, process: () => void) => {
    batch.current.push(new BatchItem(scope, process))
  }

  //////////////////////// SOCKET STATE ////////////////////////////

  // Contexts required
  const authApi = useContext(AuthContext)
  const logApi = useContext(LogContext)
  const serverApi = useContext(ServerContext)

  // Connection state
  const [connected, setConnected] = useState(false)

  //////////////////////// MARKET DATA /////////////////////////////

  // Data from the socket
  const [tickers, setTickers] = useState(Map<String, Ticker>())
  const [balances, setBalances] = useState(Map<String, Balance>())
  const [orderBook, setOrderBook] = useState<OrderBook>(null)
  const [trades, tradesUpdateApi] = useArray<Trade>(null)
  const [userTrades, userTradesUpdateApi] = useArray<UserTrade>(null)
  const [openOrders, openOrdersUpdateApi] = useOrders()

  /////////////////////// SOCKET MANAGEMENT ///////////////////////////

  const getLocation = props.getLocation
  const location = getLocation()
  const selectedCoin = useMemo(() => locationToCoin(location), [location])
  const selectedCoinTicker = useMemo(() => (selectedCoin ? tickers.get(selectedCoin.key) : null), [
    tickers,
    selectedCoin
  ])

  // Dispatch any incoming messages on the socket to state
  const logError = logApi.localError
  const logMessage = logApi.localMessage
  const logNotification = logApi.add
  const getSelectedCoin = useCallback(() => locationToCoin(getLocation()), [getLocation])
  useEffect(() => {
    socketClient.onError((message: string) => addToBatch(BatchScope.GLOBAL, () => logError(message)))
    socketClient.onNotification((logEntry: LogRequest) => addToBatch(BatchScope.GLOBAL, () => logNotification(logEntry)))

    const sameCoin = (left: Coin, right: Coin) => left && right && left.key === right.key
    socketClient.onTicker((coin: Coin, ticker: Ticker) => {
      addToBatch(BatchScope.GLOBAL, () => setTickers(tickers => tickers.set(coin.key, ticker)))
    })
    socketClient.onBalance((exchange: string, currency: string, balance: Balance) => {
      const coin = getSelectedCoin()
      if (coin && coin.exchange === exchange) {
        if (coin.base === currency) {
          addToBatch(BatchScope.COIN, () => setBalances(balances => Map.of(currency, balance, coin.counter, balances.get(coin.counter))))
        }
        if (coin.counter === currency) {
          addToBatch(BatchScope.COIN, () => setBalances(balances => Map.of(currency, balance, coin.base, balances.get(coin.base))))
        }
      }
    })
    socketClient.onOrderBook((coin: Coin, orderBook: OrderBook) => {
      if (sameCoin(coin, getSelectedCoin())) addToBatch(BatchScope.COIN, () => setOrderBook(orderBook))
    })
    socketClient.onTrade((coin: Coin, trade: Trade) => {
      if (sameCoin(coin, getSelectedCoin())) addToBatch(BatchScope.COIN, () => tradesUpdateApi.unshift(trade, { maxLength: MAX_PUBLIC_TRADES }))
    })
    socketClient.onUserTrade((coin: Coin, trade: UserTrade) => {
      if (sameCoin(coin, getSelectedCoin()))
        addToBatch(BatchScope.COIN, () => userTradesUpdateApi.unshift(trade, {
          skipIfAnyMatch: existing => !!trade.id && existing.id === trade.id
        }))
    })
    socketClient.onOrderUpdate((coin: Coin, order: Order, timestamp: number) => {
      if (sameCoin(coin, getSelectedCoin())) addToBatch(BatchScope.COIN, () => openOrdersUpdateApi.orderUpdated(order, timestamp))
    })
    socketClient.onOrdersSnapshot((coin: Coin, orders: Array<Order>, timestamp: number) => {
      if (sameCoin(coin, getSelectedCoin())) {
        addToBatch(BatchScope.COIN, () => openOrdersUpdateApi.updateSnapshot(orders, timestamp))
      }
    })
  }, [getSelectedCoin, tradesUpdateApi, userTradesUpdateApi, openOrdersUpdateApi, logError, logNotification])

  // Connect the socket when authorised, and disconnect when deauthorised
  useEffect(() => {
    if (authApi.authorised) {
      socketClient.connect()
    }
    return () => {
      console.log("Authorisation lost")
      socketClient.disconnect()
    }
  }, [authApi.authorised])

  // Sync the state of the socket with the socket itself
  useEffect(() => {
    socketClient.onConnectionStateChange((newState: boolean) => {
      console.log("Detected socket connected state", newState)
      setConnected(newState)
    })
  }, [setConnected])

  // Log when the socket connects
  useEffect(() => {
    if (connected) {
      console.log("Reconnected")
      logMessage("Socket connected")
      return () => {
        console.log("Disconnected")
        logMessage("Socket disconnected")
      }
    }
  }, [connected, logMessage])

  // When the connection state or subscription list changes, resubscribe on the socket
  const subscribedCoins = serverApi.subscriptions
  useEffect(() => {
    if (connected) {
      console.log("Resubscribing")
      socketClient.changeSubscriptions(subscribedCoins, selectedCoin)
      socketClient.resubscribe()
    }
  }, [connected, subscribedCoins, selectedCoin])

  // When the selected coin changes, clear any coin-specific state
  useEffect(() => {
    console.log("Clearing current coin state")
    setOrderBook(null)
    userTradesUpdateApi.clear()
    openOrdersUpdateApi.clear()
    tradesUpdateApi.clear()
    setBalances(Map<String, Balance>())
    clearBatchItemsForCoin()
  }, [selectedCoin, userTradesUpdateApi, tradesUpdateApi, openOrdersUpdateApi, setBalances, setOrderBook])

  const createdOrder = openOrdersUpdateApi.orderUpdated
  const pendingCancelOrder = openOrdersUpdateApi.pendingCancelOrder
  const createPlaceholder = openOrdersUpdateApi.createPlaceholder
  const removePlaceholder = openOrdersUpdateApi.removePlaceholder

  const api: SocketApi = useMemo(
    () => ({
      connected,
      tickers,
      balances,
      trades,
      userTrades,
      orderBook,
      openOrders,
      selectedCoinTicker,
      createdOrder,
      pendingCancelOrder,
      createPlaceholder,
      removePlaceholder
    }),
    [
      connected,
      tickers,
      balances,
      trades,
      userTrades,
      orderBook,
      openOrders,
      selectedCoinTicker,
      createdOrder,
      pendingCancelOrder,
      createPlaceholder,
      removePlaceholder
    ]
  )

  return <SocketContext.Provider value={api}>{props.children}</SocketContext.Provider>
}
