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
import React, { useContext } from "react"
import { connect } from "react-redux"
import OpenOrders from "../components/OpenOrders"
import AuthenticatedOnly from "./AuthenticatedOnly"
import WithCoin from "./WithCoin"
import WhileLoading from "../components/WhileLoading"
import * as jobActions from "../store/job/actions"
import { AuthContext } from "@orko-ui-auth/index"
import exchangesService from "@orko-ui-market/exchangesService"
import { Coin } from "@orko-ui-market/index"
import { SocketContext, Order } from "@orko-ui-socket/index"
import { getSelectedCoin, getJobsAsOrdersForSelectedCoin } from "selectors/coins"
import { LogContext } from "@orko-ui-log/index"

const OpenOrdersContainer: React.FC<{ jobsAsOrders: Array<Order>; coin: Coin }> = ({
  jobsAsOrders,
  coin
}) => {
  const socketApi = useContext(SocketContext)
  const authApi = useContext(AuthContext)
  const logApi = useContext(LogContext)

  const allOrders = socketApi.openOrders
  const orders = coin ? allOrders.filter(o => !o.deleted).concat(jobsAsOrders) : null

  const onCancelExchange = (id: string, coin: Coin) => {
    socketApi.pendingCancelOrder(
      id,
      // Deliberately new enough to be relevant now but get immediately overwritten
      orders.find((o: Order) => o.id === id).serverTimestamp + 1
    )
    authApi
      .authenticatedRequest(() => exchangesService.cancelOrder(coin, id))
      .catch(error => logApi.errorPopup("Could not cancel order: " + error.message))
  }

  const onCancelServer = (jobId: string) => {
    this.props.dispatch(jobActions.deleteJob(authApi, { id: jobId }))
  }

  return (
    <AuthenticatedOnly padded>
      <WithCoin padded>
        {coin => (
          <WhileLoading data={orders} padded>
            {() => (
              <OpenOrders
                orders={orders}
                onCancelExchange={(id: string) => onCancelExchange(id, coin)}
                onCancelServer={onCancelServer}
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
    jobsAsOrders: getJobsAsOrdersForSelectedCoin(state),
    coin: getSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)
