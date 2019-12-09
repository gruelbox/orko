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
import React, { useContext, useMemo } from "react"
import { connect } from "react-redux"
import OpenOrders from "../components/OpenOrders"
import AuthenticatedOnly from "./AuthenticatedOnly"
import WithCoin from "./WithCoin"
import WhileLoading from "../components/WhileLoading"
import { AuthContext } from "modules/auth"
import exchangesService from "modules/market/exchangesService"
import { Coin, ServerCoin } from "modules/market"
import { SocketContext, DisplayOrder, RunningAtType, OrderType } from "modules/socket"
import { getSelectedCoin } from "selectors/coins"
import { LogContext } from "modules/log"
import { ServerContext, OcoJob, LimitOrderJob, TradeDirection } from "modules/server"
import { isStop } from "util/jobUtils"

function jobTriggerMatchesCoin(job: OcoJob, coin: ServerCoin) {
  return (
    job.tickTrigger.exchange === coin.exchange &&
    job.tickTrigger.base === coin.base &&
    job.tickTrigger.counter === coin.counter
  )
}

const OpenOrdersContainer: React.FC<{ coin: Coin }> = ({ coin }) => {
  const socketApi = useContext(SocketContext)
  const authApi = useContext(AuthContext)
  const logApi = useContext(LogContext)
  const serverApi = useContext(ServerContext)

  const openOrders = socketApi.openOrders
  const allOrders = useMemo<DisplayOrder[]>(
    () =>
      openOrders.filter(o => !o.deleted).map(o => ({ ...o, runningAt: RunningAtType.EXCHANGE, jobId: null })),
    [openOrders]
  )
  const allJobs = serverApi.jobs
  const jobsAsOrders = useMemo<DisplayOrder[]>(() => {
    if (!coin) return []
    return allJobs
      .filter(job => isStop(job))
      .map(job => job as OcoJob)
      .filter(job => jobTriggerMatchesCoin(job, coin))
      .map(job => ({
        runningAt: RunningAtType.SERVER,
        jobId: job.id,
        type: job.high
          ? (job.high.job as LimitOrderJob).direction === TradeDirection.BUY
            ? OrderType.BID
            : OrderType.ASK
          : (job.low.job as LimitOrderJob).direction === TradeDirection.BUY
          ? OrderType.BID
          : OrderType.ASK,
        stopPrice: job.high ? Number(job.high.thresholdAsString) : Number(job.low.thresholdAsString),
        limitPrice: job.high
          ? Number((job.high.job as LimitOrderJob).limitPrice)
          : Number((job.low.job as LimitOrderJob).limitPrice),
        originalAmount: job.high
          ? Number((job.high.job as LimitOrderJob).amount)
          : Number((job.low.job as LimitOrderJob).amount),
        remainingAmount: job.high
          ? Number((job.high.job as LimitOrderJob).amount)
          : Number((job.low.job as LimitOrderJob).amount)
      }))
  }, [allJobs, coin])

  const orders: DisplayOrder[] = coin && allOrders ? allOrders.concat(jobsAsOrders) : null
  const authenticatedRequest = authApi.authenticatedRequest
  const logPopup = logApi.errorPopup
  const pendingCancelOrder = socketApi.pendingCancelOrder

  const onCancelExchange = useMemo(
    () => (id: string, coin: Coin) => {
      pendingCancelOrder(
        id,
        // Deliberately new enough to be relevant now but get immediately overwritten
        openOrders.find(o => o.id === id).serverTimestamp + 1
      )
      authenticatedRequest(() => exchangesService.cancelOrder(coin, id)).catch(error =>
        logPopup("Could not cancel order: " + error.message)
      )
    },
    [authenticatedRequest, logPopup, pendingCancelOrder, openOrders]
  )

  return (
    <AuthenticatedOnly padded>
      <WithCoin padded>
        {coin => (
          <WhileLoading data={orders} padded>
            {() => (
              <OpenOrders
                orders={orders}
                onCancelExchange={(id: string) => onCancelExchange(id, coin)}
                onCancelServer={(id: string) => serverApi.deleteJob(id)}
                coin={coin}
              />
            )}
          </WhileLoading>
        )}
      </WithCoin>
    </AuthenticatedOnly>
  )
}

function mapStateToProps(state) {
  return {
    coin: getSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)
