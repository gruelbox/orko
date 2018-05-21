import React from "react"

import ReactTable from "react-table"
import theme from "../theme"
import { pure } from 'recompose'

const style = {
  textAlign: "right"
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
  Header: "Ask"
}

const bidPriceColumn = {
  ...priceColumn,
  Header: "Bid"
}

const askColumns = [askPriceColumn, amountColumn]
const bidColumns = [amountColumn, bidPriceColumn]

const sellTrProps = {
  style: {
    color: theme.colors["sell"]
  }
}

const buyTrProps = {
  style: {
    color: theme.colors["buy"]
  }
}

const OrderBook = ({orders, direction}) => (
  <ReactTable
    data={orders}
    getTrProps={() => direction === 'BID' ? buyTrProps : sellTrProps}
    columns={direction === 'BID' ? bidColumns : askColumns}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No orders"
  />
)

export default pure(OrderBook)