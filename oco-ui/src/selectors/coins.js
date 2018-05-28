import { createSelector } from "reselect"
import { isAlert } from "../util/jobUtils"
import { coinFromKey } from "../store/coin/reducer"
import * as jobTypes from "../services/jobTypes"

const getCoins = state => state.coins.coins
const getTickers = state => state.ticker.coins
const getRouterLocation = state => state.router.location
const getJobs = state => state.job.jobs
const getOrders = state => state.coin.orders
const getOrderbook = state => state.coin.orderBook

export const getTopOfOrderBook = createSelector(
  [getOrderbook],
  orderBook => orderBook ? ({
    asks: orderBook.asks.slice(0, 16),
    bids: orderBook.bids.slice(0, 16),
  }) : orderBook
)

export const locationToCoin = ({ pathname }) => {
  if (pathname && pathname.startsWith("/coin/") && pathname.length > 6) {
    return coinFromKey(pathname.substring(6))
  } else {
    return null
  }
}

export const getSelectedCoin = createSelector([getRouterLocation], location =>
  locationToCoin(location)
)

export const getAlertJobs = createSelector(
  [getJobs],
  jobs => (jobs ? jobs.filter(job => isAlert(job)) : [])
)

export const getWatchJobsForSelectedCoin = createSelector(
  [getJobs, getSelectedCoin],
  (jobs, coin) =>
    jobs && coin
      ? jobs.filter(
          job =>
            job.jobType === jobTypes.WATCH_JOB &&
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
