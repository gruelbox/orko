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
import * as coinActions from "../coin/actions"
import * as notificationActions from "../notifications/actions"
import * as socketClient from "../../worker/socket.client.js"
import * as tickerActions from "../ticker/actions"
import * as socketActions from "../socket/actions"
import { locationToCoin } from "../../selectors/coins"
import { batchActions } from "redux-batched-actions"
import * as jobActions from "../job/actions"
import * as supportActions from "../support/actions"

var store
var deduplicatedActionBuffer = {}
var allActionBuffer = []
var initialising = true
var jobFetch
var releaseFetch
var previousCoin

function subscribedCoins() {
  return store.getState().coins.coins
}

function selectedCoin() {
  return locationToCoin(store.getState().router.location)
}

const ACTION_KEY_ORDERBOOK = "orderbook"
const ACTION_KEY_BALANCE = "balance"
const ACTION_KEY_TICKER = "ticker"

function bufferLatestAction(key, action) {
  deduplicatedActionBuffer[key] = action
}

function bufferAllActions(action) {
  allActionBuffer.push(action)
}

function clearActionsForPrefix(prefix) {
  for (const key of Object.keys(deduplicatedActionBuffer)) {
    if (key.startsWith(prefix)) delete deduplicatedActionBuffer[key]
  }
}

export function initialise(s, history) {
  store = s

  // Buffer and dispatch as a batch all the actions from the socket once a second
  const actionDispatch = () => {
    const batch = Object.values(deduplicatedActionBuffer).concat(
      allActionBuffer
    )
    deduplicatedActionBuffer = {}
    allActionBuffer = []
    store.dispatch(batchActions(batch))
  }
  setInterval(actionDispatch, 1000)

  // When the coin selected changes, send resubscription messages and clear any
  // coin-specific state
  history.listen(location => {
    const coin = locationToCoin(location)
    if (coin !== previousCoin) {
      previousCoin = coin
      console.log("Resubscribing following coin change")
      socketClient.changeSubscriptions(subscribedCoins(), coin)
      socketClient.resubscribe()
      clearActionsForPrefix(ACTION_KEY_BALANCE)
      bufferLatestAction(ACTION_KEY_ORDERBOOK, coinActions.setOrderBook(null))
      bufferAllActions(coinActions.clearUserTrades())
      store.dispatch(coinActions.clearOrders())
      bufferAllActions(coinActions.clearTrades())
      bufferAllActions(coinActions.clearBalances())
      actionDispatch()
    }
  })

  // Sync the store state of the socket with the socket itself
  socketClient.onConnectionStateChange(connected => {
    const prevState = store.getState().socket.connected
    if (prevState !== connected) {
      store.dispatch(socketActions.setConnectionState(connected))
      if (connected) {
        if (initialising) {
          store.dispatch(notificationActions.localMessage("Socket connected"))
          initialising = false
        } else {
          store.dispatch(notificationActions.localAlert("Socket reconnected"))
        }
        resubscribe()
      } else {
        store.dispatch(notificationActions.localError("Socket disconnected"))
      }
    }
  })

  // Dispatch notifications etc to the store
  socketClient.onError(message =>
    store.dispatch(notificationActions.localError(message))
  )
  socketClient.onNotification(message =>
    store.dispatch(notificationActions.add(message))
  )
  socketClient.onStatusUpdate(message =>
    store.dispatch(notificationActions.statusUpdate(message))
  )

  // Dispatch market data to the store
  const sameCoin = (left, right) => left && right && left.key === right.key
  socketClient.onTicker((coin, ticker) =>
    bufferLatestAction(
      ACTION_KEY_TICKER + "/" + coin.key,
      tickerActions.setTicker(coin, ticker)
    )
  )
  socketClient.onBalance((exchange, currency, balance) => {
    const coin = selectedCoin()
    if (
      coin &&
      coin.exchange === exchange &&
      (coin.base === currency || coin.counter === currency)
    ) {
      bufferLatestAction(
        ACTION_KEY_BALANCE + "/" + exchange + "/" + currency,
        coinActions.setBalance(exchange, currency, balance)
      )
    }
  })
  socketClient.onOrderBook((coin, orderBook) => {
    if (sameCoin(coin, selectedCoin()))
      bufferLatestAction(
        ACTION_KEY_ORDERBOOK,
        coinActions.setOrderBook(orderBook)
      )
  })
  socketClient.onTrade((coin, trade) => {
    if (sameCoin(coin, selectedCoin()))
      bufferAllActions(coinActions.addTrade(trade))
  })
  socketClient.onUserTrade((coin, trade) => {
    if (sameCoin(coin, selectedCoin()))
      bufferAllActions(coinActions.addUserTrade(trade))
  })
  socketClient.onOrderUpdate((coin, order, timestamp) => {
    if (sameCoin(coin, selectedCoin()))
      store.dispatch(coinActions.orderUpdated(order, timestamp))
  })

  // This is a bit hacky. The intent is to move this logic server side,
  // so the presence of a snapshot/poll loop is invisible to the client.
  // In the meantime, I'm not polluting the reducer with it.
  socketClient.onOrdersSnapshot((coin, orders, timestamp) => {
    if (sameCoin(coin, selectedCoin())) {
      var idsPresent = []
      if (orders.length === 0) {
        // Update that there are no orders
        store.dispatch(coinActions.orderUpdated(null, timestamp))
      } else {
        // Updates for every order mentioned
        orders.forEach(o => {
          idsPresent.push(o.id)
          store.dispatch(coinActions.orderUpdated(o, timestamp))
        })
      }

      // Any order not mentioned should be removed
      if (store.getState().coin.orders) {
        store
          .getState()
          .coin.orders.filter(o => !idsPresent.includes(o.id))
          .forEach(o => {
            store.dispatch(
              coinActions.orderUpdated(
                { id: o.id, status: "CANCELED" },
                timestamp
              )
            )
          })
      }
    }
  })
}

export function connect() {
  // Fetch and dispatch the job details on the server.
  // TODO this should really move to the socket, but for the time being
  // we'll fetch it on an interval.
  if (!jobFetch) {
    jobFetch = setInterval(() => {
      store.dispatch(jobActions.fetchJobs())
    }, 5000)
  }
  if (!releaseFetch) {
    store.dispatch(supportActions.fetchReleases())
    releaseFetch = setInterval(() => {
      store.dispatch(supportActions.fetchReleases())
    }, 180000)
  }
  socketClient.connect()
}

export function resubscribe() {
  socketClient.changeSubscriptions(subscribedCoins(), selectedCoin())
  socketClient.resubscribe()
}

export function disconnect() {
  socketClient.disconnect()
  clearInterval(jobFetch)
}
