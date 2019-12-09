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
import * as socketMessages from "./socketMessages"
import runtimeEnv from "@mars/heroku-js-runtime-env"
import ReconnectingWebSocket from "reconnecting-websocket"
import { augmentCoin, coin as createCoin, Coin } from "modules/market"
import { Ticker, Trade, OrderBook, Balance, ServerTrade, UserTrade } from "./Types"
import { LogRequest } from "modules/log"

type MessageHandler = (message: string) => void
type NotificationHandler = (logEntry: LogRequest) => void
type ConnectionStateHandler = (connected: boolean) => void
type CoinAndPayloadHandler<T> = (coin: Coin, payload: T) => void
type BalanceHandler = (exchange: string, currency: string, balance: Balance) => void

var handleError: MessageHandler = (message: string) => {}
var handleConnectionStateChange: ConnectionStateHandler = (connected: boolean) => {}
var handleNotification: NotificationHandler = (logEntry: LogRequest) => {}
var handleStatusUpdate: MessageHandler = (message: string) => {}
var handleTicker: CoinAndPayloadHandler<Ticker> = (coin: Coin, ticker: Ticker) => {}
var handleOrderBook: CoinAndPayloadHandler<OrderBook> = (coin: Coin, orderBook: OrderBook) => {}
var handleTrade: CoinAndPayloadHandler<Trade> = (coin: Coin, trade: Trade) => {}
var handleOrdersSnapshot = (coin: Coin, orders, timestamp) => {}
var handleOrderUpdate = (coin: Coin, order, timestamp) => {}
var handleUserTrade: CoinAndPayloadHandler<UserTrade> = (coin: Coin, trade: UserTrade) => {}
var handleBalance: BalanceHandler = (exchange: string, currency: string, balance: Balance) => {}

var subscribedCoins = []
var selectedCoin = null
var connected = false

var socket: ReconnectingWebSocket
var timer: NodeJS.Timeout

export function onError(handler: MessageHandler) {
  handleError = handler
}

export function onConnectionStateChange(handler: ConnectionStateHandler) {
  handleConnectionStateChange = handler
}

export function onNotification(handler: NotificationHandler) {
  handleNotification = handler
}

export function onStatusUpdate(handler: MessageHandler) {
  handleStatusUpdate = handler
}

export function onTicker(handler: CoinAndPayloadHandler<Ticker>) {
  handleTicker = handler
}

export function onOrderBook(handler: CoinAndPayloadHandler<OrderBook>) {
  handleOrderBook = handler
}

export function onTrade(handler: CoinAndPayloadHandler<Trade>) {
  handleTrade = handler
}

export function onOrdersSnapshot(handler) {
  handleOrdersSnapshot = handler
}

export function onOrderUpdate(handler) {
  handleOrderUpdate = handler
}

export function onUserTrade(handler: CoinAndPayloadHandler<UserTrade>) {
  handleUserTrade = handler
}

export function onBalance(handler: BalanceHandler) {
  handleBalance = handler
}

export function connect() {
  if (connected) return
  const root = runtimeEnv().REACT_APP_WS_URL
  console.log("Connecting to socket", root)
  socket = ws("ws", root)
  socket.addEventListener("open", () => {
    connected = true
    console.log("Socket (re)connected")
    handleConnectionStateChange(true)
    resubscribe()
  })
  socket.addEventListener("close", () => {
    connected = false
    console.log("Socket connection temporarily lost")
    handleConnectionStateChange(false)
  })
  socket.addEventListener("message", (evt: MessageEvent) => {
    var content: any
    try {
      content = JSON.parse(evt.data)
    } catch (e) {
      console.log("Failed to parse message from server (" + e + ")", evt.data)
      return
    }
    try {
      content = preProcess(content)
    } catch (e) {
      console.log("Failed to pre-process message from server (" + e + ")", evt.data)
    }
    try {
      receive(content)
    } catch (e) {
      console.log("Failed to handle message from server (" + e + ")", evt.data)
    }
  })
  timer = setInterval(() => send({ command: socketMessages.READY }), 3000)
}

export function disconnect() {
  if (connected) {
    console.log("Disconnecting socket")
    socket.close(undefined, "Shutdown")
    connected = false
    clearInterval(timer)
  }
}

export function changeSubscriptions(coins, selected) {
  if (selected) {
    subscribedCoins = coins.concat([selected])
  } else {
    subscribedCoins = coins
  }
  selectedCoin = selected
}

export function resubscribe() {
  const serverSelectedCoinTickers = selectedCoin ? [webCoinToServerCoin(selectedCoin)] : []
  send({
    command: socketMessages.CHANGE_TICKERS,
    tickers: subscribedCoins.map(coin => webCoinToServerCoin(coin))
  })
  send({
    command: socketMessages.CHANGE_OPEN_ORDERS,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: socketMessages.CHANGE_ORDER_BOOK,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: socketMessages.CHANGE_TRADES,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: socketMessages.CHANGE_ORDER_STATUS_CHANGE,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: socketMessages.CHANGE_USER_TRADES,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: socketMessages.CHANGE_BALANCE,
    tickers: serverSelectedCoinTickers
  })
  send({ command: socketMessages.UPDATE_SUBSCRIPTIONS })
}

function webCoinToServerCoin(coin) {
  return {
    exchange: coin.exchange,
    counter: coin.counter,
    base: coin.base
  }
}

function receive(message) {
  if (!message) {
    handleError("Empty event from server")
  } else {
    switch (message.nature) {
      case socketMessages.ERROR:
        console.log("Error from socket")
        handleError(message.data)
        break

      case socketMessages.TICKER:
        handleTicker(
          createCoin(message.data.spec.exchange, message.data.spec.counter, message.data.spec.base),
          message.data.ticker
        )
        break

      case socketMessages.OPEN_ORDERS:
        handleOrdersSnapshot(
          augmentCoin(message.data.spec),
          message.data.openOrders.allOpenOrders,
          message.data.timestamp
        )
        break

      case socketMessages.ORDERBOOK:
        handleOrderBook(augmentCoin(message.data.spec), message.data.orderBook)
        break

      case socketMessages.TRADE:
        handleTrade(
          augmentCoin(message.data.spec),
          new Trade(message.data.trade as ServerTrade, message.data.spec.exchange)
        )
        break

      case socketMessages.ORDER_STATUS_CHANGE:
        handleOrderUpdate(augmentCoin(message.data.spec), message.data.order, message.data.timestamp)
        break

      case socketMessages.USER_TRADE:
        handleUserTrade(
          augmentCoin(message.data.spec),
          new UserTrade(message.data.trade as ServerTrade, message.data.spec.exchange)
        )
        break

      case socketMessages.BALANCE:
        handleBalance(message.data.exchange, message.data.balance.currency, message.data.balance)
        break

      case socketMessages.NOTIFICATION:
        handleNotification(message.data)
        break

      case socketMessages.STATUS_UPDATE:
        handleStatusUpdate(message.data)
        break

      default:
        handleError("Unknown message type from server: " + message.nature)
    }
  }
}

function send(message) {
  if (connected) socket.send(JSON.stringify(message))
}

/**
 * Processor-heavy preprocessing we want to do on the incoming message
 * prior to transmitting to the main thread.
 */
function preProcess(obj) {
  switch (obj.nature) {
    case socketMessages.ORDERBOOK:
      const ORDERBOOK_SIZE = 16
      const orderBook = obj.data.orderBook
      if (orderBook.asks.length > ORDERBOOK_SIZE) {
        orderBook.asks = orderBook.asks.slice(0, 16)
      }
      if (orderBook.bids.length > ORDERBOOK_SIZE) {
        orderBook.bids = orderBook.bids.slice(0, 16)
      }
      return obj
    default:
      return obj
  }
}

function ws(url: string, root: string): ReconnectingWebSocket {
  var fullUrl: string
  if (root) {
    fullUrl = root + "/" + url
  } else {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:"
    fullUrl = protocol + "//" + window.location.host + "/" + url
  }
  console.log("Connecting", fullUrl)
  return new ReconnectingWebSocket(fullUrl)
}
