/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
import { createSelector } from "reselect"
import { getRouterLocation } from "./router"
import { getAlertJobs, getStopJobs } from "./jobs"
import { coinFromKey } from "../util/coinUtils"

const getCoins = state => state.coins.coins
const getReferencePrices = state => state.coins.referencePrices
const getTickers = state => state.ticker.coins
const getOrders = state => state.coin.orders
const getOrderbook = state => state.coin.orderBook

export const getUserTradeHistory = state => state.coin.userTradeHistory

export const getMarketTradeHistory = state => state.coin.trades

export const getTopOfOrderBook = getOrderbook // Moved to worker

export const locationToCoin = location => {
  if (
    location &&
    location.pathname &&
    location.pathname.startsWith("/coin/") &&
    location.pathname.length > 6
  ) {
    return coinFromKey(location.pathname.substring(6))
  } else {
    return null
  }
}

export const getSelectedCoin = createSelector(
  [getRouterLocation],
  location => locationToCoin(location)
)

function jobTriggerMatchesCoin(job, coin) {
  return (
    job.tickTrigger.exchange === coin.exchange &&
    job.tickTrigger.base === coin.base &&
    job.tickTrigger.counter === coin.counter
  )
}

export const getOrdersForSelectedCoin = createSelector(
  [getOrders, getStopJobs, getSelectedCoin],
  (orders, stopJobs, selectedCoin) => {
    if (!selectedCoin) return null

    var result = !orders ? [] : orders

    const server = stopJobs
      .filter(job => jobTriggerMatchesCoin(job, selectedCoin))
      .map(job => ({
        runningAt: "SERVER",
        jobId: job.id,
        type: job.high
          ? job.high.job.direction === "BUY"
            ? "BID"
            : "ASK"
          : job.low.job.direction === "BUY"
          ? "BID"
          : "ASK",
        stopPrice: job.high
          ? Number(job.high.thresholdAsString)
          : Number(job.low.thresholdAsString),
        limitPrice: job.high
          ? Number(job.high.job.limitPrice)
          : Number(job.low.job.limitPrice),
        originalAmount: job.high
          ? Number(job.high.job.amount)
          : Number(job.low.job.amount),
        cumulativeAmount: "--"
      }))

    result = result.concat(server)

    if (result.length === 0 && !orders) return null

    return result
  }
)

export const getSelectedCoinTicker = createSelector(
  [getSelectedCoin, getTickers],
  (coin, tickers) => (coin ? tickers[coin.key] : null)
)

export const getCoinsForDisplay = createSelector(
  [getAlertJobs, getCoins, getTickers, getReferencePrices],
  (alertJobs, coins, tickers, referencePrices) =>
    coins.map(coin => {
      const referencePrice = referencePrices[coin.key]
      const ticker = tickers[coin.key]
      return {
        ...coin,
        ticker,
        hasAlert: !!alertJobs.find(
          job =>
            job.tickTrigger.exchange === coin.exchange &&
            job.tickTrigger.base === coin.base &&
            job.tickTrigger.counter === coin.counter
        ),
        priceChange: referencePrice
          ? Number(
              (((ticker ? ticker.last : referencePrice) - referencePrice) *
                100) /
                referencePrice
            ).toFixed(2) + "%"
          : "--"
      }
    })
)
