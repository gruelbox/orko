import React from "react"
import { connect } from "react-redux"
import Loading from "../components/primitives/Loading"
import OpenOrders from "../components/OpenOrders"
import * as coinActions from "../store/coin/actions"
import * as jobActions from "../store/job/actions"
import { getOrdersWithWatchesForSelectedCoin } from "../selectors/coins"

class OpenOrdersContainer extends React.Component {

  onCancel = (id, orderType) => {
    this.props.dispatch(coinActions.cancelOrder(this.props.coin, id, orderType))
  }

  onWatch = (id, watchJob) => {
    if (watchJob) {
      this.props.dispatch(jobActions.deleteJob(watchJob))
    } else {
      this.props.dispatch(jobActions.submitWatchJob(this.props.coin, id))
    }
  }

  render() {
    return !this.props.orders ? (
      <Loading p={2} />
    ) : (
      <OpenOrders
        orders={this.props.orders}
        onCancel={this.onCancel}
        onWatch={this.onWatch}
      />
    )
  }
}

function mapStateToProps(state, props) {
  const orders = getOrdersWithWatchesForSelectedCoin(state)
  return {
    orders: orders ? orders.asMutable() : null
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)