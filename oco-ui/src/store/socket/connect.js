import * as coinActions from "../coin/actions"
import * as errorActions from "../error/actions"
import * as notificationActions from "../notifications/actions"
import * as socketClient from '../../worker/socket.client.js'
import * as tickerActions from "../ticker/actions"
import * as socketActions from "../socket/actions"
import { locationToCoin } from "../../selectors/coins"

var store

function authToken() {
  return store.getState().auth.token
}

function subscribedCoins() {
  return store.getState().coins.coins
}

function selectedCoin() {
  return locationToCoin(store.getState().router.location)
}

export function initialise(s, history) {
  store = s
  history.listen(location => {
    console.log("Resubscribing following coin change")
    store.dispatch(coinActions.setOrderBook(null))
    store.dispatch(coinActions.setOrders(null))
    store.dispatch(coinActions.setTradeHistory(null))
    store.dispatch(coinActions.clearBalances())
    socketClient.changeSubscriptions(subscribedCoins(), locationToCoin(location))
    socketClient.resubscribe()
  })
  socketClient.onConnectionStateChange(connected => {
    store.dispatch(socketActions.setConnectionState(connected))
    if (connected) {
      console.log("Resubscribing following reconnection")
      resubscribe()
    }
  })
  socketClient.onClearErrors(() => store.dispatch(errorActions.clearBackground("ws")))
  socketClient.onError(message => store.dispatch(errorActions.addBackground(message, "ws")))
  socketClient.onNotification(message => store.dispatch(notificationActions.add(message)))
  socketClient.onTicker((coin, ticker) => store.dispatch(tickerActions.setTicker(coin, ticker)))
  socketClient.onBalance((exchange, currency, balance) => store.dispatch(coinActions.setBalance(exchange, currency, balance)))

  const sameCoin = (left, right) => left && right && left.key === right.key

  socketClient.onOrderBook((coin, orderBook) => {
    if (sameCoin(coin, selectedCoin()))
      store.dispatch(coinActions.setOrderBook(orderBook))
  })
  socketClient.onOrders((coin, orders) => {
    if (sameCoin(coin, selectedCoin()))
      store.dispatch(coinActions.setOrders(orders))
  })
  socketClient.onTradeHistory((coin, trades) => {
    if (sameCoin(coin, selectedCoin()))
      store.dispatch(coinActions.setTradeHistory(trades))
  })
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