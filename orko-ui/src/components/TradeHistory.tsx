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
import Price from "../components/primitives/Price"
import Amount from "../components/primitives/Amount"
import * as dateUtils from "modules/common/util/dateUtils"
import { Trade, UserTrade, OrderType } from "modules/socket"

const BUY_SIDE = OrderType.BID

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const dateColumn = {
  id: "date",
  Header: "Created",
  Cell: ({ original }: { original: Trade }) => dateUtils.formatDate(original.timestamp),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 80
}

const orderTypeColumn = {
  id: "orderType",
  Header: <Icon fitted name="sort" title="Direction" />,
  Cell: ({ original }: { original: Trade }) => (
    <Icon
      fitted
      name={original.type === BUY_SIDE ? "arrow up" : "arrow down"}
      title={original.type === BUY_SIDE ? "Buy" : "Sell"}
    />
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  width: 32
}

const priceColumn = {
  Header: "Price",
  Cell: ({ original }: { original: Trade }) => (
    <Price coin={original.coin} color={original.type === BUY_SIDE ? "buy" : "sell"} noflash bare>
      {original.price}
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
  Cell: ({ original }: { original: Trade }) => (
    <Amount coin={original.coin} color={original.type === BUY_SIDE ? "buy" : "sell"} noflash bare>
      {original.originalAmount}
    </Amount>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const feeAmountColumn = {
  Header: "Fee",
  Cell: ({ original }: { original: Trade }) => (
    <Amount color={original.type === BUY_SIDE ? "buy" : "sell"} noflash bare noValue="--">
      {original instanceof UserTrade ? (original as UserTrade).feeAmount : null}
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
  accessor: "feeCurrency",
  headerStyle: numberStyle,
  style: numberStyle,
  sortable: false,
  resizable: true,
  minWidth: 50
}

const columns = (excludeFees: boolean) =>
  excludeFees
    ? [orderTypeColumn, dateColumn, priceColumn, amountColumn]
    : [orderTypeColumn, dateColumn, priceColumn, amountColumn, feeAmountColumn, feeCurrencyColumn]

const TradeHistory: React.FC<{ trades: Array<Trade>; excludeFees: boolean }> = ({ trades, excludeFees }) => (
  <ReactTable
    data={trades}
    getTrProps={(state, rowInfo) => ({
      className: rowInfo.original.type === BUY_SIDE ? "oco-buy" : "oco-sell"
    })}
    columns={columns(excludeFees)}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No trade history"
    defaultPageSize={1000}
  />
)

export default TradeHistory
