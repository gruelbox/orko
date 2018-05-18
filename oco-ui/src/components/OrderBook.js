import React from "react"

import ReactTable from "react-table"
import theme from "../theme"

const style = {
  textAlign: "right",
}

const amountColumn = {
  id: "amount",
  Header: "Amount",
  accessor: "remainingAmount",
  headerStyle: style,
  style: style,
  minWidth: 80
}

const priceColumn = {
  id: "limitPrice",
  accessor: "limitPrice",
  headerStyle: style,
  style: style,
  minWidth: 80
}

const askPriceColumn = {
  ...priceColumn,
  Header: "Ask",
}

const bidPriceColumn = {
  ...priceColumn,
  Header: "Bid",
}

const askColumns = [askPriceColumn, amountColumn]
const bidColumns = [amountColumn, bidPriceColumn]

const OrderBook = ({orders, direction}) => (
  <ReactTable
    data={orders}
    getTrProps={(state, rowInfo, column) => ({
      style: {
        color: theme.colors[direction === 'BID' ? "buy" : "sell"]
      }
    })}
    columns={direction === 'BID' ? bidColumns : askColumns}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No orders"
  />
)

export default OrderBook