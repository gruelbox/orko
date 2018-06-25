import React from "react"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import Price from "../components/primitives/Price"
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
  Cell: ({ original }) => dateUtils.formatDate(original.d),
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
    <Icon fitted name={original.t === "BID" ? "arrow up" : "arrow down"} title={original.t === "BID" ? "Buy" : "Sell"}/>
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  width: 32
}

const priceColumn = {
  Header: "Price",
  Cell: ({ original }) => (
    <Price counter={original.c.counter} color={original.t === "BID" ? "buy" : "sell"} noflash bare>
      {original.p}
    </Price>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const amountColumn ={
  Header: "Amount",
  Cell: ({ original }) => (
    <Price color={original.t === "BID" ? "buy" : "sell"} noflash bare>
      {original.a}
    </Price>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const feeAmountColumn = {
  Header: "Fee",
  Cell: ({ original }) => (
    <Price color={original.t === "BID" ? "buy" : "sell"} noflash bare>
      {original.fa}
    </Price>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const feeCurrencyColumn = {
  Header: "Fee Ccy",
  accessor: "fc",
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const TradeHistory = props => (
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
      feeAmountColumn,
      feeCurrencyColumn
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No trade history"
    defaultPageSize={1000}
  />
)

export default TradeHistory