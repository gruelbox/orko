import { createSelector } from "reselect"
import { isAlert } from "../util/jobUtils"
import { coinFromKey } from "../store/coin/reducer"
import * as jobTypes from "../services/jobTypes"

const getCoins = state => state.coins.coins
const getTickers = state => state.ticker.coins
const getRouterPath = state => state.router.location.pathname
const getJobs = state => state.job.jobs
const getOrders = state => state.coin.orders

export const getSelectedCoin = createSelector(
  [getRouterPath],
  (path) => {
    if (path && path.startsWith("/coin/") && path.length > 6) {
      return coinFromKey(path.substring(6))
    } else {
      return null
    }
  }
)

export const getAlertJobs = createSelector(
  [getJobs],
  (jobs) => jobs ? jobs.filter(job => isAlert(job)) : []
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
      : null,
)

export const getSelectedCoinTicker = createSelector(
  [getSelectedCoin, getTickers],
  (coin, tickers) => coin ? tickers[coin.key] : null
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