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
import React from "react"
import { connect } from "react-redux"
import Loading from "../components/primitives/Loading"
import OpenOrders from "../components/OpenOrders"
import * as coinActions from "../store/coin/actions"
import * as jobActions from "../store/job/actions"
import { getOrdersForSelectedCoin, getSelectedCoin } from "../selectors/coins"

class OpenOrdersContainer extends React.Component {
  onCancelExchange = (id, orderType) => {
    this.props.dispatch(coinActions.cancelOrder(this.props.coin, id, orderType))
  }

  onCancelServer = jobId => {
    this.props.dispatch(jobActions.deleteJob({ id: jobId }))
  }

  render() {
    return !this.props.orders ? (
      <Loading p={2} />
    ) : (
      <OpenOrders
        orders={this.props.orders}
        onCancelExchange={this.onCancelExchange}
        onCancelServer={this.onCancelServer}
        onWatch={this.onWatch}
        coin={this.coin}
      />
    )
  }
}

function mapStateToProps(state, props) {
  return {
    orders: getOrdersForSelectedCoin(state),
    coin: getSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)
