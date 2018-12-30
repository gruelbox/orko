import React from "react"
import Chart from "../components/Chart"
import { getSelectedCoin } from "../selectors/coins"
import { connect } from "react-redux"

class ChartContainer extends React.Component {
  render() {
    return <Chart coin={this.props.coin} onHide={this.props.onHide} />
  }
}

export default connect((state, props) => ({
  coin: getSelectedCoin(state)
}))(ChartContainer)
