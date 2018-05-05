import React from "react"
import { connect } from "react-redux"

import ReactTable from "react-table"
import Section from "../components/primitives/Section"

const textStyle = {
  textAlign: "left",
}

const NotificationsContainer = ({notifications, dispatch}) => (
  <Section id="notifications" heading="Notifications" nopadding>
    <ReactTable
      data={notifications}
      columns={[
        {
          id: "type",
          Header: "Type",
          accessor: "notificationType",
          headerStyle: textStyle,
          style: textStyle,
          resizable: true,
          width: 100
        },
        {
          id: "message",
          Header: "Message",
          accessor: "message",
          headerStyle: textStyle,
          style: textStyle,
          resizable: true
        },
      ]}
      showPagination={false}
      resizable={false}
      className="-striped"
      minRows={0}
    />
  </Section>
)

function mapStateToProps(state) {
  return {
    notifications: state.notifications.notifications.asMutable().reverse()
  }
}

export default connect(mapStateToProps)(NotificationsContainer)