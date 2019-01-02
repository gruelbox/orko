import React from "react"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import Price from "../components/primitives/Price"
import Amount from "../components/primitives/Amount"
import * as dateUtils from "../util/dateUtils"

const BUY_SIDE = "BID"

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
  Header: <Icon fitted name="sort" title="Direction" />,
  accessor: "t",
  Cell: ({ original }) => (
    <Icon
      fitted
      name={original.t === BUY_SIDE ? "arrow up" : "arrow down"}
      title={original.t === BUY_SIDE ? "Buy" : "Sell"}
    />
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  width: 32
}

const priceColumn = coin => ({
  Header: "Price",
  Cell: ({ original }) => (
    <Price
      coin={coin}
      color={original.t === BUY_SIDE ? "buy" : "sell"}
      noflash
      bare
    >
      {original.p}
    </Price>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
})

const amountColumn = coin => ({
  Header: "Amount",
  Cell: ({ original }) => (
    <Amount
      coin={coin}
      color={original.t === BUY_SIDE ? "buy" : "sell"}
      noflash
      bare
    >
      {original.a}
    </Amount>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
})

const feeAmountColumn = {
  Header: "Fee",
  Cell: ({ original }) => (
    <Amount
      color={original.t === BUY_SIDE ? "buy" : "sell"}
      noflash
      bare
      noValue="--"
    >
      {original.fa}
    </Amount>
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

const columns = (coin, excludeFees) =>
  excludeFees
    ? [orderTypeColumn, dateColumn, priceColumn(coin), amountColumn(coin)]
    : [
        orderTypeColumn,
        dateColumn,
        priceColumn(coin),
        amountColumn(coin),
        feeAmountColumn,
        feeCurrencyColumn
      ]

const TradeHistory = props => (
  <ReactTable
    data={props.trades}
    getTrProps={(state, rowInfo, column) => ({
      className: rowInfo.original.t === BUY_SIDE ? "oco-buy" : "oco-sell"
    })}
    columns={columns(props.coin, props.excludeFees)}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No trade history"
    defaultPageSize={1000}
  />
)

export default TradeHistory
