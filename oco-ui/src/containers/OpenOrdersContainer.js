import React from "react"
import { connect } from "react-redux"

import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"

import Section from "../components/primitives/Section"
import Para from "../components/primitives/Para"
import Panel from "../components/primitives/Panel"
import Href from "../components/primitives/Href"
import Loading from "../components/primitives/Loading"
import FlashEntry from "../components/primitives/FlashEntry"

import * as coinActions from "../store/coin/actions"
import * as jobActions from "../store/job/actions"
import * as dateUtils from "../util/dateUtils"
import { getOrdersWithWatchesForSelectedCoin } from "../selectors/coins"

const NoCoin = props => (
  <Panel p={2}>
    <Para>No coin selected</Para>
  </Panel>
)

const NoData = props => (
  <Panel p={2}>
    <Para>No market data for {props.coin.name}</Para>
  </Panel>
)

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const orderTypeColumn = {
  id: "orderType",
  Header: <Icon fitted name="sort" title="Direction"/>,
  accessor: "type",
  Cell: ({ original }) => (
    <FlashEntry>
      <Icon fitted name={original.type === "BID" ? "arrow up" : "arrow down"} title={original.type === "BID" ? "Buy" : "Sell"}/>
    </FlashEntry>
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  width: 32
}

const runningAtColumn = {
  id: "runningAt",
  Header: "At",
  Cell: ({ original }) => (
    <FlashEntry>
      <Icon fitted name="server" title="On exchange. Will execute immediately but locks up the balance."/>
    </FlashEntry>
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  width: 32
}

const createdDateColumn = {
  id: "createdDate",
  accessor: "timestamp",
  Header: "Created",
  Cell: ({ original }) => (
    <FlashEntry content={dateUtils.formatDate(original.timestamp)} />
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 80
}

const limitPriceColumn = {
  Header: "Limit",
  Cell: ({ original }) => <FlashEntry content={original.limitPrice} />,
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const stopPriceColumn = {
  id: "stopPrice",
  Header: "Trigger",
  Cell: ({ original }) => (
    <FlashEntry content={original.stopPrice ? original.stopPrice : "-"} />
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const amountColumn = {
  Header: "Amount",
  Cell: ({ original }) => (
    <FlashEntry content={original.originalAmount} />
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const filledColumn = {
  Header: "Filled",
  Cell: ({ original }) => (
    <FlashEntry content={original.cumulativeAmount} />
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const Orders = props => (
  <ReactTable
    data={props.orders.asMutable()}
    getTrProps={(state, rowInfo, column) => ({
      className: rowInfo.original.type === "BID" ? "oco-buy" : "oco-sell"
    })}
    columns={[
      {
        id: "close",
        Header: () => null,
        Cell: ({ original }) => (
          <FlashEntry>
            <Href
              onClick={() => props.onCancel(original.id, original.type)}
              title="Cancel order"
            >
              <Icon fitted name="close" />
            </Href>
          </FlashEntry>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 32,
        sortable: false,
        resizable: false
      },
      orderTypeColumn,
      runningAtColumn,
      createdDateColumn,
      limitPriceColumn,
      stopPriceColumn,
      amountColumn,
      filledColumn,
      {
        id: "watch",
        Header: <Icon fitted name="eye" />,
        Cell: ({ original }) => (
          <FlashEntry>
            <Href
              onClick={() => props.onWatch(original.id, original.watchJob)}
              title={original.watchJob ? "Remove watch" : "Add watch"}
            >
              <Icon
                fitted
                name={original.watchJob ? "eye" : "circle outline"}
              />
            </Href>
          </FlashEntry>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 32,
        sortable: false,
        resizable: false
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

  componentWillReceiveProps(nextProps) {
    const nextKey = nextProps.coin ? nextProps.coin.key : null
    const thisKey = this.props.coin ? this.props.coin.key : null
    if (nextKey !== thisKey) {
      this.setState({ loading: true })
    } else {
      this.setState({ loading: false })
    }
  }

  render() {
    var component = this.state.loading ? (
      <Loading p={2} />
    ) : !this.props.coin ? (
      <NoCoin />
    ) : this.props.ordersUnavailable ? (
      <NoData coin={this.props.coin} />
    ) : !this.props.orders ? (
      <Loading />
    ) : (
      <Orders
        orders={this.props.orders}
        onCancel={this.onCancel}
        onWatch={this.onWatch}
      />
    )

    return (
      <Section nopadding id="orders" heading="Open Orders">
        {component}
      </Section>
    )
  }
}

function mapStateToProps(state, props) {
  return {
    orders: getOrdersWithWatchesForSelectedCoin(state),
    ordersUnavailable: state.coin.ordersUnavailable
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)
