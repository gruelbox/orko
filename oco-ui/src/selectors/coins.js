import { createSelector } from "reselect"
import { isAlert } from "../util/jobUtils"

const getAlertJobs = state =>
  state.job && state.job.jobs ? state.job.jobs.filter(job => isAlert(job)) : []
const getCoins = state => state.coins.coins
const getTickers = state => state.ticker.coins

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