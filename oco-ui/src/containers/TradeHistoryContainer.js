import React from "react"
import { connect } from "react-redux"

import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"

import Loading from "../components/primitives/Loading"
import FlashEntry from "../components/primitives/FlashEntry"
import { getTradeHistoryInReverseOrder } from "../selectors/coins"

import * as dateUtils from "../util/dateUtils"

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const dateColumn = {
  id: "date",
  accessor: "d",
  Header: "Created",
  Cell: ({ original }) => (
    <FlashEntry content={dateUtils.formatDate(original.d)} />
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 80
}

const orderTypeColumn = {
  id: "orderType",
  Header: <Icon fitted name="sort" title="Direction"/>,
  accessor: "t",
  Cell: ({ original }) => (
    <FlashEntry>
      <Icon fitted name={original.t === "BID" ? "arrow up" : "arrow down"} title={original.t === "BID" ? "Buy" : "Sell"}/>
    </FlashEntry>
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  width: 32
}

const priceColumn = {
  Header: "Price",
  Cell: ({ original }) => <FlashEntry content={original.p} />,
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const amountColumn = {
  Header: "Amount",
  Cell: ({ original }) => (
    <FlashEntry content={original.a} />
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const feeColumn = {
  Header: "Fee",
  Cell: ({ original }) => (
    <FlashEntry content={original.fa + " " + original.fc} />
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const Trades = props => (
  <ReactTable
    data={props.trades}
    getTrProps={(state, rowInfo, column) => ({
      className: rowInfo.original.t === "BID" ? "oco-buy" : "oco-sell"
    })}
    columns={[
      orderTypeColumn,
      dateColumn,
      priceColumn,
      amountColumn,
      feeColumn
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No trade history"
  />
)

class TradeHistoryContainer extends React.Component {
  render() {
    return !this.props.tradeHistory ? (
      <Loading p={2} />
    ) : (
      <Trades trades={this.props.tradeHistory} />
    )
  }
}

function mapStateToProps(state, props) {
  const tradeHistory = getTradeHistoryInReverseOrder(state);
  return {
    tradeHistory: tradeHistory ? tradeHistory.slice(0) : null,
    unavailable: state.coin.tradeHistoryUnavailable
  }
}

export default connect(mapStateToProps)(TradeHistoryContainer)