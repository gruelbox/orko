import React from "react"
import { connect } from "react-redux"

import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"

import Section from "../components/primitives/Section"
import Para from "../components/primitives/Para"
import Panel from "../components/primitives/Panel"
import Table from "../components/primitives/Table"
import Cell from "../components/primitives/Cell"
import HeaderCell from "../components/primitives/HeaderCell"
import Row from "../components/primitives/Row"
import Href from "../components/primitives/Href"
import Loading from "../components/primitives/Loading"

import * as coinActions from "../store/coin/actions"

const TICK_TIME = 10000

const NoData = props => (
  <Panel p={2}>
    <Para>No market data for {props.coin.name}</Para>
  </Panel>
)

const NoOrders = props => (
  <Panel p={2}>
    <Para>No open orders</Para>
  </Panel>
)

const formatDate = timestamp => {
  var d = new Date(timestamp)
  return d.toLocaleDateString() + " " + d.toLocaleTimeString()
}

const textStyle = {
  textAlign: "left",
}

const numberStyle = {
  textAlign: "right",
}

const Orders = props => (
  <ReactTable
    data={props.orders.allOpenOrders}
    columns={[
      {
        id: "close",
        Header: () => <Icon name="close" />,
        Cell: ({original}) => (
          <Href onClick={() => props.onCancel(original.id, original.type)}>
            <Icon name="close" />
          </Href>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 36
      },
      {
        id: "createdDate",
        Header: "Created",
        Cell: ({original}) => formatDate(original.timestamp),
        headerStyle: textStyle,
        style: textStyle,
        resizable: true
      },
      {
        id: "orderType",
        Header: "Direction",
        Cell: ({original}) => original.type === "BID" ? "Buy" : "Sell",
        headerStyle: textStyle,
        style: textStyle,
        resizable: true
      },
      {
        Header: "Limit",
        accessor: "limitPrice",
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true
      },
      {
        id: "stopPrice",
        Header: "Trigger",
        Cell: ({original}) => original.stopPrice ? original.stopPrice : "-",
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true
      },
      {
        Header: "Amount",
        accessor: "originalAmount",
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true
      },
      {
        Header: "Filled",
        accessor: "cumulativeAmount",
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true
      },
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No open orders"
  />
)

class OpenOrdersContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = { loading: true }
  }

  tick = () => {
    this.props.dispatch(coinActions.fetchOrders(this.props.coin))
  }

  onCancel = (id, orderType) => {
    this.props.dispatch(coinActions.cancelOrder(this.props.coin, id, orderType))
  }

  componentDidMount() {
    this.tick()
    this.interval = setInterval(this.tick, TICK_TIME)
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.coin.key !== this.props.coin.key) {
      this.setState({ loading: true }, () => this.tick())
    } else {
      this.setState({ loading: false })
    }
  }

  render() {
    var component = this.state.loading ? (
      <Loading p={2}/>
    ) : this.props.ordersUnavailable ? (
      <NoData coin={this.props.coin} />
    ) : !this.props.orders ? (
      <Loading />
    ) : this.props.orders.allOpenOrders.length === 0 ? (
      <NoOrders />
    ) : (
      <Orders orders={this.props.orders} onCancel={this.onCancel} />
    )

    return (
      <Section nopadding id="orders" heading="Open Orders">
        {component}
      </Section>
    )
  }
}

function mapStateToProps(state) {
  return {
    orders: state.coin.orders,
    ordersUnavailable: state.coin.ordersUnavailable
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)
