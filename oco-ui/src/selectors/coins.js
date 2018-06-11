import { createSelector } from "reselect"
import { getRouterLocation } from "./router"
import { getAlertJobs, getWatchJobs, getStopJobs } from "./jobs"
import { coinFromKey } from "../util/coinUtils"

const getCoins = state => state.coins.coins
const getTickers = state => state.ticker.coins
const getOrders = state => state.coin.orders
const getOrderbook = state => state.coin.orderBook
const getTradeHistory = state => state.coin.tradeHistory

export const getTopOfOrderBook = getOrderbook // Moved to worker

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

function jobTriggerMatchesCoin(job, coin) {
  return job.tickTrigger.exchange === coin.exchange &&
          job.tickTrigger.base === coin.base &&
          job.tickTrigger.counter === coin.counter
}

export const getWatchJobsForSelectedCoin = createSelector(
  [getWatchJobs, getSelectedCoin],
  (jobs, coin) =>
    jobs && coin
      ? jobs.filter(job => jobTriggerMatchesCoin(job, coin))
      : []
)

export const getOrdersWithWatchesForSelectedCoin = createSelector(
  [getOrders, getStopJobs, getWatchJobsForSelectedCoin, getSelectedCoin],
  (orders, stopJobs, watchJobs, selectedCoin) => {

    if (!selectedCoin)
      return null

    const exchange = orders
      ? orders.allOpenOrders.map(order => {
          const watchJob = watchJobs.find(job => job.orderId === order.id)
          if (watchJob) {
            return { ...order, watchJob }
          } else {
            return order
          }
        })
      : []

    const server = stopJobs
      .filter(job => jobTriggerMatchesCoin(job, selectedCoin))
      .map(job => ({
        runningAt: "SERVER",
        jobId: job.id,
        type: job.high
          ? job.high.job.direction === "BUY" ? "BID" : "ASK"
          : job.low.job.direction === "BUY" ? "BID" : "ASK",
        stopPrice: job.high ? Number(job.high.thresholdAsString) : Number(job.low.thresholdAsString),
        limitPrice: job.high ? Number(job.high.job.bigDecimals.limitPrice) : Number(job.low.job.bigDecimals.limitPrice),
        originalAmount: job.high ? Number(job.high.job.bigDecimals.amount) : Number(job.low.job.bigDecimals.amount),
        cumulativeAmount: "--"
      }))

    return exchange.concat(server)
  }
)

export const getTradeHistoryInReverseOrder = getTradeHistory // Moved to worker

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