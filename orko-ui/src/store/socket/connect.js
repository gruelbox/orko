import * as coinActions from "../coin/actions"
import * as notificationActions from "../notifications/actions"
import * as socketClient from "../../worker/socket.client.js"
import * as tickerActions from "../ticker/actions"
import * as socketActions from "../socket/actions"
import { locationToCoin } from "../../selectors/coins"
import { batchActions } from "redux-batched-actions"
import * as jobActions from "../job/actions"

var store

var actionBuffer = {}

function authToken() {
  return store.getState().auth.token
}

function subscribedCoins() {
  return store.getState().coins.coins
}

function selectedCoin() {
  return locationToCoin(store.getState().router.location)
}

const ACTION_KEY_ORDERBOOK = "orderbook"
const ACTION_KEY_ORDERS = "orders"
const ACTION_KEY_TRADE = "trade"
const ACTION_KEY_BALANCE = "balance"
const ACTION_KEY_TICKER = "ticker"

function bufferAction(key, action) {
  actionBuffer[key] = action
}

export function initialise(s, history) {
  store = s

  // Buffer and dispatch as a batch all the actions from the socket once a second
  const actionDispatch = () => {
    const batch = Object.values(actionBuffer)
    actionBuffer = {}
    store.dispatch(batchActions(batch))
  }
  setInterval(actionDispatch, 1000)

  // When the coin selected changes, send resubscription messages and clear any
  // coin-specific state
  history.listen(location => {
    console.log("Resubscribing following coin change")
    const coin = locationToCoin(location)
    socketClient.changeSubscriptions(subscribedCoins(), coin)
    socketClient.resubscribe()
    store.dispatch(coinActions.setUserTrades(null))
    bufferAction(ACTION_KEY_ORDERBOOK, coinActions.setOrderBook(null))
    bufferAction(ACTION_KEY_ORDERS, coinActions.setOrders(null))
    bufferAction(ACTION_KEY_TRADE, coinActions.clearTrades())
    bufferAction(ACTION_KEY_BALANCE, coinActions.clearBalances())
    actionDispatch()
  })

  // Sync the store state of the socket with the socket itself
  socketClient.onConnectionStateChange(connected => {
    store.dispatch(socketActions.setConnectionState(connected))
    if (connected) {
      if (!store.socket || store.socket.connected !== connected)
        store.dispatch(notificationActions.localMessage("Socket connected"))
      resubscribe()
    } else {
      if (!store.socket || store.socket.connected !== connected)
        store.dispatch(notificationActions.localError("Socket disconnected"))
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
    bufferAction(
      ACTION_KEY_TICKER + "/" + coin.key,
      tickerActions.setTicker(coin, ticker)
    )
  )
  socketClient.onBalance((exchange, currency, balance) =>
    bufferAction(
      ACTION_KEY_BALANCE + "/" + exchange + "/" + currency,
      coinActions.setBalance(exchange, currency, balance)
    )
  )
  socketClient.onOrderBook((coin, orderBook) => {
    if (sameCoin(coin, selectedCoin()))
      bufferAction(ACTION_KEY_ORDERBOOK, coinActions.setOrderBook(orderBook))
  })
  socketClient.onOrders((coin, orders) => {
    if (sameCoin(coin, selectedCoin()))
      bufferAction(
        ACTION_KEY_ORDERS,
        coinActions.setOrders(orders.allOpenOrders)
      )
  })
  socketClient.onTrade((coin, trade) => {
    if (sameCoin(coin, selectedCoin()))
      bufferAction(ACTION_KEY_TRADE + trade.id, coinActions.addTrade(trade))
  })
  socketClient.onUserTrade((coin, trade) => {
    if (sameCoin(coin, selectedCoin()))
      store.dispatch(coinActions.addUserTrade(trade))
  })
  socketClient.onUserTradeHistory((coin, trades) => {
    if (sameCoin(coin, selectedCoin()))
      store.dispatch(coinActions.setUserTrades(trades))
  })

  // Fetch and dispatch the job details on the server.
  // TODO this should really move to the socket, but for the time being
  // we'll fetch it on an interval.
  setInterval(() => {
    store.dispatch(jobActions.fetchJobs())
  }, 5000)
}

export function connect() {
  socketClient.connect(authToken())
}

export function resubscribe() {
  socketClient.changeSubscriptions(subscribedCoins(), selectedCoin())
  socketClient.resubscribe()
}

export function disconnect() {
  socketClient.disconnect()
}
