import { createSelector } from "reselect"
import { getRouterLocation } from "./router"
import { getAlertJobs, getStopJobs } from "./jobs"
import { coinFromKey } from "../util/coinUtils"

const getCoins = state => state.coins.coins
const getTickers = state => state.ticker.coins
const getOrders = state => state.coin.orders
const getOrderbook = state => state.coin.orderBook
const getUserTradeHistory = state => state.coin.userTradeHistory

export const getMarketTradeHistory = state => state.coin.trades

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

export const getOrdersForSelectedCoin = createSelector(
  [getOrders, getStopJobs, getSelectedCoin],
  (orders, stopJobs, selectedCoin) => {

    if (!selectedCoin)
      return null

    const exchange = orders
      ? orders.allOpenOrders
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

export const getTradeHistoryInReverseOrder = getUserTradeHistory // Moved to worker

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