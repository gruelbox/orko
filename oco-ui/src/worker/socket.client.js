import Worker from './socket.worker.js'
import runtimeEnv from "@mars/heroku-js-runtime-env"
import * as socketEvents from "./socketEvents"
import * as serverMessages from "./socketMessages"
import { coin as createCoin } from "../store/coin/reducer"
import { augmentCoin } from "../store/coin/reducer"

var handleError = message => {}
var handleClearErrors = () => {}
var handleConnectionStateChange = connected => {}
var handleNotification = message => {}
var handleTicker = (coin, ticker) => {}
var handleOrders = (coin, orders) => {}
var handleOrderBook = (coin, orderBook) => {}
var handleTradeHistory = (coin, trades) => {}

var subscribedCoins = []
var selectedCoin = null
var connected = false

const worker = new Worker()
worker.onmessage = m => receive(JSON.parse(m.data))

export function onError(handler) {
  handleError = handler
}

export function onClearErrors(handler) {
  handleClearErrors = handler
}

export function onConnectionStateChange(handler) {
  handleConnectionStateChange = handler
}

export function onNotification(handler) {
  handleNotification = handler
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

export function onTradeHistory(handler) {
  handleTradeHistory = handler
}

export function connect(token) {
  if (connected)
    throw Error("Already connected")
  const root = runtimeEnv().REACT_APP_WS_URL
  console.log("Connecting to socket", root)
  worker.postMessage({ eventType: socketEvents.CONNECT, payload: { token, root }})
}

export function disconnect() {
  if (connected)
    worker.postMessage({ eventType: socketEvents.DISCONNECT })
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
  send({
    command: serverMessages.CHANGE_TICKERS,
    tickers: subscribedCoins.map(coin => webCoinToServerCoin(coin))
  })
  send({
    command: serverMessages.CHANGE_OPEN_ORDERS,
    tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
  })
  send({
    command: serverMessages.CHANGE_ORDER_BOOK,
    tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
  })
  send({
    command: serverMessages.CHANGE_TRADE_HISTORY,
    tickers: selectedCoin ? [ webCoinToServerCoin(selectedCoin) ] : []
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

        handleClearErrors()
        handleTicker(
          createCoin(message.data.spec.exchange, message.data.spec.counter, message.data.spec.base),
          message.data.ticker
        )
        break
      
      case serverMessages.OPEN_ORDERS:
      case serverMessages.ORDERBOOK:
      case serverMessages.TRADE_HISTORY:

        const coin = augmentCoin(message.data.spec)
        handleClearErrors()
        if (message.nature === serverMessages.OPEN_ORDERS) {
          handleOrders(coin, message.data.openOrders)
        } else if (message.nature === serverMessages.ORDERBOOK) {
          handleOrderBook(coin, message.data.orderBook)
        } else if (message.nature === serverMessages.TRADE_HISTORY) {
          handleTradeHistory(coin, message.data.trades)
        }
        break
      
      case serverMessages.NOTIFICATION:

        handleNotification(message.data)
        break

      default:
        handleError("Unknown message type from server: " + message.nature)
    }

  } else {
    handleError("Unknown event from server: " + JSON.stringify(event))
  }
}