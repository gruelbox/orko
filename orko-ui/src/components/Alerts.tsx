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
import Href from "../components/primitives/Href"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import theme from "../theme"

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const highStyle = {
  textAlign: "right",
  color: theme.colors.buy
}

const lowStyle = {
  textAlign: "right",
  color: theme.colors.sell
}

const lowPriceColumn = {
  id: "lowPrice",
  Header: "Low price",
  Cell: ({ original }) => (original.low ? original.low.thresholdAsString : "--"),
  headerStyle: numberStyle,
  style: lowStyle,
  resizable: true,
  minWidth: 50
}

const highPriceColumn = {
  id: "highPrice",
  Header: "High price",
  Cell: ({ original }) => (original.high ? original.high.thresholdAsString : "--"),
  headerStyle: numberStyle,
  style: highStyle,
  resizable: true,
  minWidth: 50
}

const defaultSort = [
  {
    id: "lowPrice",
    desc: false
  },
  {
    id: "highPrice",
    desc: false
  }
]

interface AlertsProps {
  alerts
  onDelete(job): void
}

const Alerts: React.FC<AlertsProps> = ({ alerts, onDelete }) => (
  <ReactTable
    data={alerts}
    style={{
      border: "1px solid rgba(0,0,0,0.3)"
    }}
    defaultSorted={defaultSort}
    columns={[
      {
        id: "close",
        Header: null,
        Cell: ({ original }) => (
          <Href title="Remove alert" onClick={() => onDelete(original)}>
            <Icon fitted name="close" />
          </Href>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 32,
        sortable: false,
        resizable: false
      },
      lowPriceColumn,
      highPriceColumn
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No alerts"
  />
)

export default Alerts
