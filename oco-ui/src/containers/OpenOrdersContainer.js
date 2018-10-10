import React from "react"
import { connect } from "react-redux"
import Loading from "../components/primitives/Loading"
import OpenOrders from "../components/OpenOrders"
import * as coinActions from "../store/coin/actions"
import * as jobActions from "../store/job/actions"
import { getOrdersForSelectedCoin } from "../selectors/coins"

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
    orders: getOrdersForSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)
