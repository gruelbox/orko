import React from "react"
import { connect } from "react-redux"
import Loading from "../components/primitives/Loading"
import OpenOrders from "../components/OpenOrders"
import * as coinActions from "../store/coin/actions"
import * as jobActions from "../store/job/actions"
import { getOrdersWithWatchesForSelectedCoin } from "../selectors/coins"

class OpenOrdersContainer extends React.Component {

  onCancelExchange = (id, orderType) => {
    this.props.dispatch(coinActions.cancelOrder(this.props.coin, id, orderType))
  }

  onCancelServer = (jobId) => {
    this.props.dispatch(jobActions.deleteJob({ id: jobId }))
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
        onCancelExchange={this.onCancelExchange}
        onCancelServer={this.onCancelServer}
        onWatch={this.onWatch}
      />
    )
  }
}

function mapStateToProps(state, props) {
  return {
    orders: getOrdersWithWatchesForSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)