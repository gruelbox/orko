import Worker from "./socket.worker.js"
import runtimeEnv from "@mars/heroku-js-runtime-env"
import * as socketEvents from "./socketEvents"
import * as serverMessages from "./socketMessages"
import { augmentCoin, coin as createCoin } from "../util/coinUtils"

var handleError = message => {}
var handleConnectionStateChange = connected => {}
var handleNotification = message => {}
var handleStatusUpdate = message => {}
var handleTicker = (coin, ticker) => {}
var handleOrders = (coin, orders) => {}
var handleOrderBook = (coin, orderBook) => {}
var handleTrade = (coin, trade) => {}
var handleUserTrade = (coin, trade) => {}
var handleUserTradeHistory = (coin, trades) => {}
var handleBalance = (exchange, currency, balance) => {}

var subscribedCoins = []
var selectedCoin = null
var connected = false

const worker = new Worker()
worker.onmessage = m => receive(JSON.parse(m.data))

export function onError(handler) {
  handleError = handler
}

export function onConnectionStateChange(handler) {
  handleConnectionStateChange = handler
}

export function onNotification(handler) {
  handleNotification = handler
}

export function onStatusUpdate(handler) {
  handleStatusUpdate = handler
}

export function onTicker(handler) {
  handleTicker = handler
}

export function onOrders(handler) {
  handleOrders = handler
}

export function onOrderBook(handler) {
  handleOrderBook = handler
}

export function onTrade(handler) {
  handleTrade = handler
}

export function onUserTrade(handler) {
  handleUserTrade = handler
}

export function onUserTradeHistory(handler) {
  handleUserTradeHistory = handler
}

export function onBalance(handler) {
  handleBalance = handler
}

export function connect(token) {
  if (connected) throw Error("Already connected")
  const root = runtimeEnv().REACT_APP_WS_URL
  console.log("Connecting to socket", root)
  worker.postMessage({
    eventType: socketEvents.CONNECT,
    payload: { token, root }
  })
}

export function disconnect() {
  if (connected) {
    console.log("Disconnecting socket")
    worker.postMessage({ eventType: socketEvents.DISCONNECT })
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
  const serverSelectedCoinTickers = selectedCoin
    ? [webCoinToServerCoin(selectedCoin)]
    : []
  send({
    command: serverMessages.CHANGE_TICKERS,
    tickers: subscribedCoins.map(coin => webCoinToServerCoin(coin))
  })
  send({
    command: serverMessages.CHANGE_OPEN_ORDERS,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: serverMessages.CHANGE_ORDER_BOOK,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: serverMessages.CHANGE_TRADES,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: serverMessages.CHANGE_USER_TRADE_HISTORY,
    tickers: serverSelectedCoinTickers
  })
  send({
    command: serverMessages.CHANGE_BALANCE,
    tickers: serverSelectedCoinTickers
  })
  send({ command: serverMessages.UPDATE_SUBSCRIPTIONS })
}

function webCoinToServerCoin(coin) {
  return {
    exchange: coin.exchange,
    counter: coin.counter,
    base: coin.base
  }
}

function send(message) {
  worker.postMessage({
    eventType: socketEvents.MESSAGE,
    payload: message
  })
}

function receive(event) {
  if (!event) {
    handleError("Empty event from server")
  } else if (event.eventType === socketEvents.OPEN) {
    console.log("Socket (re)connected")
    connected = true
    handleConnectionStateChange(true)
    resubscribe()
  } else if (event.eventType === socketEvents.CLOSE) {
    console.log("Socket connection temporarily lost")
    connected = false
    handleConnectionStateChange(false)
  } else if (event.eventType === socketEvents.MESSAGE) {
    const message = event.payload
    switch (message.nature) {
      case serverMessages.ERROR:
        console.log("Error from socket")
        handleError(message.data)
        break

      case serverMessages.TICKER:
        handleTicker(
          createCoin(
            message.data.spec.exchange,
            message.data.spec.counter,
            message.data.spec.base
          ),
          message.data.ticker
        )
        break

      case serverMessages.OPEN_ORDERS:
        handleOrders(augmentCoin(message.data.spec), message.data.openOrders)
        break

      case serverMessages.ORDERBOOK:
        handleOrderBook(augmentCoin(message.data.spec), message.data.orderBook)
        break

      case serverMessages.TRADE:
        handleTrade(augmentCoin(message.data.spec), message.data.trade)
        break

      case serverMessages.USER_TRADE:
        handleUserTrade(augmentCoin(message.data.spec), message.data.trade)
        break

      case serverMessages.USER_TRADE_HISTORY:
        handleUserTradeHistory(
          augmentCoin(message.data.spec),
          message.data.trades
        )
        break

      case serverMessages.BALANCE:
        handleBalance(
          message.data.exchange,
          message.data.currency,
          message.data.balance
        )
        break

      case serverMessages.NOTIFICATION:
        handleNotification(message.data)
        break

      case serverMessages.STATUS_UPDATE:
        handleStatusUpdate(message.data)
        break

      default:
        handleError("Unknown message type from server: " + message.nature)
    }
  } else {
    handleError("Unknown event from server: " + JSON.stringify(event))
  }
}
