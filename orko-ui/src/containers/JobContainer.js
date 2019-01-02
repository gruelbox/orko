/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
import React from "react"
import { connect } from "react-redux"

import Job from "../components/Job"

import FixedModal from "../components/primitives/FixedModal"
import Para from "../components/primitives/Para"
import { Modal, Icon } from "semantic-ui-react"

class JobContainer extends React.Component {
  render() {
    const job = this.props.jobs.find(
      j => j.id === this.props.match.params.jobId
    )

    return (
      <FixedModal
        data-orko={"job/" + this.props.match.params.jobId}
        closeIcon
        onClose={() => this.props.history.push("/")}
        style={{ height: "100%" }}
      >
        <Modal.Header>
          <Icon name="code" />
          {"Job " + this.props.match.params.jobId}
        </Modal.Header>
        <Modal.Content scrolling>
          {job && <Job job={job} />}
          {!job && <Para>Job {this.props.match.params.jobId} not found</Para>}
        </Modal.Content>
      </FixedModal>
    )
  }
}

function mapStateToProps(state) {
  return {
    jobs: state.job.jobs
  }
}

export default connect(mapStateToProps)(JobContainer)
