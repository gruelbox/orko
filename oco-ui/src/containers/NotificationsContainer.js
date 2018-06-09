import React from "react"
import { connect } from "react-redux"

import * as dateUtils from '../util/dateUtils'
import * as notificationActions from '../store/notifications/actions'

import ReactTable from "react-table"
import Section from "../components/primitives/Section"
import Href from "../components/primitives/Href"
import Span from "../components/primitives/Span"
import { Icon } from "semantic-ui-react"
import theme from "../theme"

const textStyle = {
  textAlign: "left",
}

const columns = [
  {
    id: "icon",
    Header: null,
    accessor: "notificationType",
    Cell: ({original}) => original.notificationType === "ERROR" ? <Icon fitted name="warning sign"/> : <Icon fitted name="info"/>,
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    width: 32
  },
  {
    id: "dateTime",
    Header: "Time",
    accessor: "dateTime",
    Cell: ({original}) => dateUtils.formatDate(original.dateTime),
    headerStyle: textStyle,
    style: textStyle,
    resizable: true,
    width: 130
  },
  {
    id: "message",
    Header: "Message",
    accessor: "message",
    Cell: ({original}) => original.message,
    headerStyle: textStyle,
    style: textStyle,
    resizable: true
  },
]

const NotificationsContainer = ({notifications, dispatch}) => (
  <Section id="notifications" heading="Server Notifications" nopadding buttons={
    () => (
      <Span color="white">
        <Href title="Clear notifications" onClick={() => dispatch(notificationActions.clear())}><Icon name="close"/></Href>
      </Span>
    )
  }>
    <ReactTable
      data={notifications}
      getTrProps={(state, rowInfo, column) => ({
        style: {
          color: rowInfo.original.notificationType === "ERROR" ? theme.colors.alert : undefined
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