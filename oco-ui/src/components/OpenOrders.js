import React from "react"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import Href from "../components/primitives/Href"
import FlashEntry from "../components/primitives/FlashEntry"
import * as dateUtils from "../util/dateUtils"

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
    <FlashEntry>
      <Icon
        fitted
        name={original.type === "BID" ? "arrow up" : "arrow down"}
        title={original.type === "BID" ? "Buy" : "Sell"}
      />
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
      <Icon
        fitted
        name="server"
        title="On exchange. Will execute immediately but locks up the balance."
      />
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
  Cell: ({ original }) => <FlashEntry content={original.originalAmount} />,
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const filledColumn = {
  Header: "Filled",
  Cell: ({ original }) => <FlashEntry content={original.cumulativeAmount} />,
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const OpenOrders = props => (
  <ReactTable
    data={props.orders}
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
      }
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No open orders"
  />
)

export default OpenOrders