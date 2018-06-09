import { createSelector } from "reselect"
import { getRouterLocation } from "./router"
import { getAlertJobs } from "./jobs"
import { getWatchJobs } from "./jobs"
import { coinFromKey } from "../store/coin/reducer"

const getCoins = state => state.coins.coins
const getTickers = state => state.ticker.coins
const getOrders = state => state.coin.orders
const getOrderbook = state => state.coin.orderBook
const getTradeHistory = state => state.coin.tradeHistory

export const getTopOfOrderBook = createSelector(
  [getOrderbook],
  orderBook => orderBook ? ({
    asks: orderBook.asks.slice(0, 16),
    bids: orderBook.bids.slice(0, 16),
  }) : orderBook
)

export const locationToCoin = (location) => {
  if (location && location.pathname && location.pathname.startsWith("/coin/") && location.pathname.length > 6) {
    return coinFromKey(location.pathname.substring(6))
  } else {
    return null
  }
}

export const getSelectedCoin = createSelector([getRouterLocation], location =>
  locationToCoin(location)
)

export const getWatchJobsForSelectedCoin = createSelector(
  [getWatchJobs, getSelectedCoin],
  (jobs, coin) =>
    jobs && coin
      ? jobs.filter(
          job =>
            job.tickTrigger.exchange === coin.exchange &&
            job.tickTrigger.base === coin.base &&
            job.tickTrigger.counter === coin.counter
        )
      : []
)

export const getOrdersWithWatchesForSelectedCoin = createSelector(
  [getOrders, getWatchJobsForSelectedCoin],
  (orders, watchJobs) =>
    orders
      ? orders.allOpenOrders.map(order => {
          const watchJob = watchJobs.find(job => job.orderId === order.id)
          if (watchJob) {
            return { ...order, watchJob }
          } else {
            return order
          }
        })
      : null
)

export const getTradeHistoryInReverseOrder = createSelector(
  [getTradeHistory],
  (tradeHistory) => tradeHistory ? tradeHistory.asMutable().reverse() : null
)

export const getSelectedCoinTicker = createSelector(
  [getSelectedCoin, getTickers],
  (coin, tickers) => (coin ? tickers[coin.key] : null)
)

export const getCoinsForDisplay = createSelector(
  [getAlertJobs, getCoins, getTickers],
  (alertJobs, coins, tickers) =>
    coins.map(coin => ({
      ...coin,
      ticker: tickers[coin.key],
      hasAlert: !!alertJobs.find(
        job =>
          job.tickTrigger.exchange === coin.exchange &&
          job.tickTrigger.base === coin.base &&
          job.tickTrigger.counter === coin.counter
      )
    }))
)