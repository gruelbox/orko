import runtimeEnv from "@mars/heroku-js-runtime-env"
import * as socketMessages from "./socketMessages"
import ReconnectingWebSocket from "reconnecting-websocket"
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

var socket
var timer

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

export function connect() {
  if (connected) throw Error("Already connected")
  const root = runtimeEnv().REACT_APP_WS_URL
  console.log("Connecting to socket", root)
  socket = ws("ws", root)
  socket.onopen = () => {
    connected = true
    console.log("Socket (re)connected")
    handleConnectionStateChange(true)
    resubscribe()
  }
  socket.onclose = () => {
    connected = false
    console.log("Socket connection temporarily lost")
    handleConnectionStateChange(false)
  }
  socket.onmessage = evt => {
    try {
      receive(preProcess(JSON.parse(evt.data)))
    } catch (e) {
      console.log("Invalid message from server", evt.data)
    }
  }
  timer = setInterval(() => send({ command: socketMessages.READY }), 3000)
}

export function disconnect() {
  if (connected) {
    console.log("Disconnecting socket")
    socket.close(undefined, "Shutdown", { keepClosed: true })
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
  const serverSelectedCoinTickers = selectedCoin
    ? [webCoinToServerCoin(selectedCoin)]
    : []
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
    command: socketMessages.CHANGE_USER_TRADE_HISTORY,
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
          createCoin(
            message.data.spec.exchange,
            message.data.spec.counter,
            message.data.spec.base
          ),
          message.data.ticker
        )
        break

      case socketMessages.OPEN_ORDERS:
        handleOrders(augmentCoin(message.data.spec), message.data.openOrders)
        break

      case socketMessages.ORDERBOOK:
        handleOrderBook(augmentCoin(message.data.spec), message.data.orderBook)
        break

      case socketMessages.TRADE:
        handleTrade(augmentCoin(message.data.spec), message.data.trade)
        break

      case socketMessages.USER_TRADE:
        handleUserTrade(augmentCoin(message.data.spec), message.data.trade)
        break

      case socketMessages.USER_TRADE_HISTORY:
        handleUserTradeHistory(
          augmentCoin(message.data.spec),
          message.data.trades
        )
        break

      case socketMessages.BALANCE:
        handleBalance(
          message.data.exchange,
          message.data.currency,
          message.data.balance
        )
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

    case socketMessages.USER_TRADE_HISTORY:
      obj.data.trades = obj.data.trades.sort((a, b) => b.d - a.d)
      return obj

    case socketMessages.USER_TRADE:
      obj.data.trade.t = obj.data.trade.t === "BID" ? "ASK" : "BID"
      return obj

    default:
      return obj
  }
}

function ws(url, root) {
  var fullUrl
  if (root) {
    fullUrl = root + "/" + url
  } else {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:"
    fullUrl = protocol + "//" + window.location.host + "/" + url
  }
  console.log("Connecting", fullUrl)
  return new ReconnectingWebSocket(fullUrl)
}
