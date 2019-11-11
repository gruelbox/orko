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
import React, { useContext } from "react"
import { connect } from "react-redux"

import JobComponent from "../components/JobComponent"

import FixedModal from "../components/primitives/FixedModal"
import Para from "../components/primitives/Para"
import { Modal, Icon } from "semantic-ui-react"
import { ServerContext } from "modules/server"

const JobContainer: React.FC<any> = ({ match, history }) => {
  const jobId = match.params.jobId // from uri
  const serverApi = useContext(ServerContext)
  const job = serverApi.jobs.find(j => j.id === jobId)
  return (
    <FixedModal
      data-orko={"job/" + jobId}
      closeIcon
      onClose={() => history.push("/")}
      style={{ height: "100%" }}
    >
      <Modal.Header>
        <Icon name="code" />
        {"Job " + jobId}
      </Modal.Header>
      <Modal.Content scrolling>
        {job && <JobComponent job={job} />}
        {!job && <Para>Job {jobId} not found</Para>}
      </Modal.Content>
    </FixedModal>
  )
}

export default connect(() => {})(JobContainer)
