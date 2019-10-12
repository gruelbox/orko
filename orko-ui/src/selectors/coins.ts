/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import { createSelector, OutputSelector } from "reselect"
import { getRouterLocation } from "./router"
import { getStopJobs } from "./jobs"
import { coinFromKey } from "modules/market/coinUtils"
import { Coin, ServerCoin } from "modules/market/Types"
import { Order } from "modules/socket"

export const locationToCoin = (location: Location): Coin => {
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

export const getSelectedCoin: OutputSelector<any, Coin, (res1: any, res2: any) => Coin> = createSelector(
  [getRouterLocation],
  location => locationToCoin(location)
)

function jobTriggerMatchesCoin(job, coin: ServerCoin) {
  return (
    job.tickTrigger.exchange === coin.exchange &&
    job.tickTrigger.base === coin.base &&
    job.tickTrigger.counter === coin.counter
  )
}

export const getJobsAsOrdersForSelectedCoin: OutputSelector<
  any,
  Coin,
  (res1: any, res2: any) => Array<Order>
> = createSelector(
  [getStopJobs, getSelectedCoin],
  (stopJobs, selectedCoin) => {
    if (!selectedCoin) return []
    return stopJobs
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
        stopPrice: job.high ? Number(job.high.thresholdAsString) : Number(job.low.thresholdAsString),
        limitPrice: job.high ? Number(job.high.job.limitPrice) : Number(job.low.job.limitPrice),
        originalAmount: job.high ? Number(job.high.job.amount) : Number(job.low.job.amount),
        cumulativeAmount: "--"
      }))
  }
)
