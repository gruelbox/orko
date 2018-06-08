import React from "react"

import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"

import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"
import Price from "../components/primitives/Price"

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const exchangeColumn = {
  id: "exchange",
  Header: "Exchange",
  accessor : "exchange",
  Cell: ({ original }) => (
    <Link to={"/coin/" + original.key} title="Open coin">
      {original.exchange}
    </Link>
  ),
  headerStyle: textStyle,
  style: textStyle,
  resizable: true,
  minWidth: 50,
}

const nameColumn = {
  id: "name",
  Header: "Name",
  accessor: "shortName",
  Cell: ({ original }) => (
    <Link to={"/coin/" + original.key} title="Open coin">
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
  Cell: ({ original }) => (
    <Price counter={original.counter} bare>
      {original.ticker ? original.ticker.last : undefined}
    </Price>
  ),
  headerStyle: numberStyle,
  style: numberStyle,
  resizable: true,
  minWidth: 50,
  sortable: false,
}

const closeColumn = (onRemove) => ({
  id: "close",
  Header: null,
  Cell: ({ original }) => (
    <Href title="Remove coin" onClick={() => onRemove(original)}>
      <Icon fitted name="close" />
    </Href>
  ),
  headerStyle: textStyle,
  style: textStyle,
  width: 32,
  sortable: false,
  resizable: false
})

const alertColumn =(onClickAlerts) => ({
  id: "alert",
  Header: <Icon fitted name="bell outline" />,
  Cell: ({ original }) => (
    <Href title="Manage alerts" onClick={() => onClickAlerts(original)}>
      <Icon
        fitted
        name={original.hasAlert ? "bell" : "bell outline"}
      />
    </Href>
  ),
  headerStyle: textStyle,
  style: textStyle,
  width: 32,
  sortable: false,
  resizable: false
})

const Coins = ({ data, onRemove, onClickAlerts }) => (
  <ReactTable
    data={data}
    columns={[
      closeColumn(onRemove),
      exchangeColumn,
      nameColumn,
      priceColumn,
      alertColumn(onClickAlerts)
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="Add a coin by clicking +, above"
  />
)

export default Coins