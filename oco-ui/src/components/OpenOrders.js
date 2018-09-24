import React from "react"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import Href from "../components/primitives/Href"
import * as dateUtils from "../util/dateUtils"
import Price from "../components/primitives/Price"

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const orderTypeColumn = {
  id: "orderType",
  Header: <Icon fitted name="sort" title="Direction" />,
  accessor: "type",
  Cell: ({ original }) => (
    <Icon
      fitted
      name={original.type === "BID" ? "arrow up" : "arrow down"}
      title={original.type === "BID" ? "Buy" : "Sell"}
    />
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
    original.runningAt === "SERVER"
      ? (
          <Icon
            fitted
            name="desktop"
            title="On server. Slightly delayed execution which may cause slippage, but does not lock the balance."
          />
        )
      : (
          <Icon
            fitted
            name="server"
            title="On exchange. Will execute immediately but locks up the balance."
          />
        )
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
  Cell: ({ original }) => original.timestamp ? dateUtils.formatDate(original.timestamp) : "Unknown",
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 80
}

const limitPriceColumn = {
  Header: "Limit",
  Cell: ({ original }) => (
    <Price color={original.type === "BID" ? "buy" : "sell"} noflash bare>
      {original.limitPrice}
    </Price>
  ),
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
    <Price color={original.type === "BID" ? "buy" : "sell"} noflash bare>
      {original.stopPrice ? original.stopPrice : "--"}
    </Price>
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
    <Price color={original.type === "BID" ? "buy" : "sell"} noflash bare>
      {original.originalAmount}
    </Price>
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
    <Price color={original.type === "BID" ? "buy" : "sell"} noflash bare>
      {original.cumulativeAmount}
    </Price>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const cancelColumn = (onCancelExchange, onCancelServer) => ({
  id: "close",
  Header: () => null,
  Cell: ({ original }) => (
    <Href
      onClick={() => {
        if (original.runningAt === "SERVER") {
          onCancelServer(original.jobId)
        } else {
          onCancelExchange(original.id, original.type)
        }
      }}
      title="Cancel order"
    >
      <Icon fitted name="close" />
    </Href>
  ),
  headerStyle: textStyle,
  style: textStyle,
  width: 32,
  sortable: false,
  resizable: false
})

const OpenOrders = props => (
  <ReactTable
    data={props.orders}
    getTrProps={(state, rowInfo, column) => ({
      className: rowInfo.original.type === "BID" ? "oco-buy" : "oco-sell"
    })}
    columns={[
      cancelColumn(props.onCancelExchange, props.onCancelServer),
      orderTypeColumn,
      runningAtColumn,
      createdDateColumn,
      limitPriceColumn,
      stopPriceColumn,
      amountColumn,
      filledColumn
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No open orders"
    defaultPageSize={1000}
  />
)

export default OpenOrders