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

import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"
import Price from "../components/primitives/Price"
import { Exchange, Coin } from "modules/market"
import { Ticker } from "modules/socket"

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const exchangeColumn = {
  id: "exchange",
  Header: "Exchange",
  accessor: "exchange",
  Cell: ({ original }: { original: FullCoinData }) => (
    <Link data-orko={original.key + "/exchange"} to={"/coin/" + original.key} title="Open coin">
      {original.exchangeMeta ? original.exchangeMeta.name : original.exchange}
    </Link>
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 40
}

const nameColumn = {
  id: "name",
  Header: "Name",
  accessor: "shortName",
  Cell: ({ original }: { original: FullCoinData }) => (
    <Link data-orko={original.key + "/name"} to={"/coin/" + original.key} title="Open coin">
      {original.shortName}
    </Link>
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 50
}

const priceColumn = {
  id: "price",
  Header: "Price",
  Cell: ({ original }: { original: FullCoinData }) => (
    <Price data-orko={original.key + "/price"} coin={original} bare>
      {original.ticker ? original.ticker.last : undefined}
    </Price>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  resizable: true,
  minWidth: 56,
  sortable: false
}

const changeColumn = (onClick: CoinCallback) => ({
  id: "change",
  Header: "Change",
  accessor: "change",
  Cell: ({ original }: { original: FullCoinData }) => (
    <Href
      data-orko={original.key + "/setReferencePrice"}
      color={original.priceChange.slice(0, 1) === "-" ? "sell" : "buy"}
      onClick={() => onClick(original)}
      title="Set reference price"
    >
      {original.priceChange}
    </Href>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  resizable: true,
  minWidth: 40
})

const closeColumn = (onRemove: CoinCallback) => ({
  id: "close",
  Header: null,
  Cell: ({ original }: { original: FullCoinData }) => (
    <Href data-orko={original.key + "/remove"} title="Remove coin" onClick={() => onRemove(original)}>
      <Icon fitted name="close" />
    </Href>
  ),
  headerStyle: textStyle,
  style: textStyle,
  width: 32,
  sortable: false,
  resizable: false
})

const alertColumn = (onClickAlerts: CoinCallback) => ({
  id: "alert",
  Header: <Icon fitted name="bell outline" />,
  Cell: ({ original }: { original: FullCoinData }) => (
    <Href data-orko={original.key + "/alerts"} title="Manage alerts" onClick={() => onClickAlerts(original)}>
      <Icon fitted name={original.hasAlert ? "bell" : "bell outline"} />
    </Href>
  ),
  headerStyle: textStyle,
  style: textStyle,
  width: 32,
  sortable: false,
  resizable: false
})

export const DATA_ATTRIBUTE = "data-orko"

interface FullCoinData {
  exchangeMeta: Exchange
  ticker: Ticker
  hasAlert: boolean
  priceChange: string
  key: string
  name: string
  shortName: string
  exchange: string
  base: string
  counter: string
}

export type CoinCallback = (coin: Coin) => void

interface CoinsProps {
  data: FullCoinData[]
  onRemove: CoinCallback
  onClickAlerts: CoinCallback
  onClickReferencePrice: CoinCallback
}

const Coins: React.FC<CoinsProps> = ({ data, onRemove, onClickAlerts, onClickReferencePrice }) => (
  <ReactTable
    data={data}
    getTrProps={(state, { original }: { original: FullCoinData }) => ({
      [DATA_ATTRIBUTE]: "coin/" + original.key
    })}
    columns={[
      closeColumn(onRemove),
      exchangeColumn,
      nameColumn,
      priceColumn,
      changeColumn(onClickReferencePrice),
      alertColumn(onClickAlerts)
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="Add a coin by clicking +, above"
    defaultPageSize={1000}
  />
)

export default Coins
