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
import { connect } from "react-redux"

import * as dateUtils from "../util/dateUtils"
import * as notificationActions from "../store/notifications/actions"

import ReactTable from "react-table"
import Section from "../components/primitives/Section"
import Href from "../components/primitives/Href"
import Span from "../components/primitives/Span"
import { Icon } from "semantic-ui-react"
import theme from "../theme"

const textStyle = {
  textAlign: "left"
}

const iconStyle = {
  textAlign: "center"
}

const columns = [
  {
    id: "icon",
    Header: null,
    accessor: "notificationType",
    Cell: ({ original }) =>
      original.level === "ERROR" ? (
        <Icon fitted name="warning sign" />
      ) : original.level === "ALERT" ? (
        <Icon fitted name="check circle outline" />
      ) : (
        <Icon fitted name="info" />
      ),
    headerStyle: iconStyle,
    style: iconStyle,
    resizable: true,
    width: 32
  },
  {
    id: "dateTime",
    Header: "Time",
    accessor: "dateTime",
    Cell: ({ original }) => dateUtils.formatDate(original.dateTime),
    headerStyle: textStyle,
    style: textStyle,
    resizable: false,
    width: 134
  },
  {
    id: "message",
    Header: "Message",
    accessor: "message",
    Cell: ({ original }) => (
      <div title={original.message}>{original.message}</div>
    ),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true
  }
]

const NotificationsContainer = ({ notifications, dispatch }) => (
  <Section
    id="notifications"
    heading="Server Notifications"
    nopadding
    buttons={() => (
      <Span color="white">
        <Href
          title="Clear notifications"
          onClick={() => dispatch(notificationActions.clear())}
        >
          <Icon name="trash" />
        </Href>
      </Span>
    )}
  >
    <ReactTable
      data={notifications}
      getTrProps={(state, rowInfo, column) => ({
        style: {
          color:
            rowInfo.original.level === "ERROR"
              ? theme.colors.alert
              : rowInfo.original.level === "ALERT"
              ? theme.colors.emphasis
              : rowInfo.original.level === "TRACE"
              ? theme.colors.deemphasis
              : undefined
        }
      })}
      columns={columns}
      showPagination={false}
      resizable={false}
      className="-striped"
      minRows={0}
      noDataText="No new notifications"
    />
  </Section>
)

export default connect(state => ({
  notifications: state.notifications.notifications
}))(NotificationsContainer)
