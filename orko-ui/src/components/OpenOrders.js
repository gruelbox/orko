/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React from "react"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import Href from "../components/primitives/Href"
import * as dateUtils from "modules/common/util/dateUtils"
import Price from "../components/primitives/Price"
import Amount from "../components/primitives/Amount"

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const DATA_ATTRIBUTE = "data-orko"
const DATA_TYPE = "data-type"

const orderTypeColumn = {
  id: "orderType",
  Header: <Icon fitted name="sort" title="Direction" />,
  accessor: "type",
  Cell: ({ original }) => (
    <Icon
      fitted
      name={!!original.stopPrice ? "stop circle" : original.type === "BID" ? "arrow up" : "arrow down"}
      title={(!!original.stopPrice ? "STOP " : "") + (original.type === "BID" ? "Buy" : "Sell")}
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
  Cell: ({ original }) =>
    original.runningAt === "SERVER" ? (
      <Icon
        fitted
        name="desktop"
        title="On server. Slightly delayed execution which may cause slippage, but does not lock the balance."
      />
    ) : (
      <Icon fitted name="server" title="On exchange. Will execute immediately but locks up the balance." />
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
  Cell: ({ original }) =>
    original.timestamp
      ? dateUtils.formatDate(original.timestamp)
      : original.runningAt === "SERVER"
      ? "Not on exchange"
      : "Confirming...",
  getProps: () => ({
    [DATA_ATTRIBUTE]: "createdDate"
  }),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 80
}

const limitPriceColumn = coin => ({
  Header: "Limit",
  Cell: ({ original }) =>
    !!original.stopPrice && !original.limitPrice ? (
      "MARKET"
    ) : (
      <Price data-orko="limitPrice" color={original.type === "BID" ? "buy" : "sell"} noflash bare coin={coin}>
        {original.limitPrice}
      </Price>
    ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
})

const stopPriceColumn = coin => ({
  id: "stopPrice",
  Header: "Trigger",
  Cell: ({ original }) => (
    <Price data-orko="stopPrice" color={original.type === "BID" ? "buy" : "sell"} noflash bare coin={coin}>
      {original.stopPrice ? original.stopPrice : "--"}
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
    <Amount data-orko="amount" color={original.type === "BID" ? "buy" : "sell"} noflash bare coin={coin}>
      {original.originalAmount}
    </Amount>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
})

const filledColumn = coin => ({
  Header: "Filled",
  Cell: ({ original }) => (
    <Amount data-orko="filled" color={original.type === "BID" ? "buy" : "sell"} noflash bare coin={coin}>
      {original.cumulativeAmount == null ? "--" : original.cumulativeAmount}
    </Amount>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
})

const cancelColumn = (onCancelExchange, onCancelServer) => ({
  id: "close",
  Header: () => null,
  Cell: ({ original }) =>
    original.status === "CANCELED" ? null : (
      <Href
        data-orko="cancel"
        onClick={() => {
          if (original.runningAt === "SERVER") {
            onCancelServer(original.jobId)
          } else {
            onCancelExchange(original.id)
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
      className:
        (rowInfo.original.type === "BID" ? "oco-buy" : "oco-sell") + " oco-" + rowInfo.original.status,
      [DATA_ATTRIBUTE]: "openOrder/" + rowInfo.original.id,
      [DATA_TYPE]: "openOrder/" + (rowInfo.original.type === "BID" ? "buy" : "sell")
    })}
    columns={[
      cancelColumn(props.onCancelExchange, props.onCancelServer),
      orderTypeColumn,
      runningAtColumn,
      createdDateColumn,
      limitPriceColumn(props.coin),
      stopPriceColumn(props.coin),
      amountColumn(props.coin),
      filledColumn(props.coin)
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
