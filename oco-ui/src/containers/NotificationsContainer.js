import React from "react"
import { connect } from "react-redux"
import Immutable from 'seamless-immutable'

import * as dateUtils from '../util/dateUtils'

import ReactTable from "react-table"
import Section from "../components/primitives/Section"
import FlashEntry from "../components/primitives/FlashEntry"
import { Icon } from "semantic-ui-react"

const textStyle = {
  textAlign: "left",
}

const NotificationsContainer = ({notifications, dispatch}) => (
  <Section id="notifications" heading="Notifications" nopadding>
    <ReactTable
      data={Immutable.asMutable(notifications)}
      defaultSorted={[
        {
          id: "dateTime",
          desc: false
        }
      ]}
      columns={[
        {
          id: "type",
          Header: "Type",
          Cell: ({original}) => (
            <FlashEntry>
              <span>
                {original.notificationType === "ERROR" ? <Icon name="warning sign"/> : <Icon name="info"/>}
                {original.notificationType}
              </span>
            </FlashEntry>
          ),
          headerStyle: textStyle,
          style: textStyle,
          resizable: true,
          width: 75
        },
        {
          id: "dateTime",
          Header: "Time",
          Cell: ({original}) => (dateUtils.formatDate(original.dateTime)),
          headerStyle: textStyle,
          style: textStyle,
          resizable: true,
          width: 130
        },
        {
          id: "message",
          Header: "Message",
          Cell: ({original}) => <FlashEntry content={original.message} />,
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
    notifications: state.notifications.notifications
  }
}

export default connect(mapStateToProps)(NotificationsContainer)