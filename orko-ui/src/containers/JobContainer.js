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
        onClose={() => this.props.history.goBack()}
        style={{ height: "100%" }}
      >
        <Modal.Header>
          <Icon name="code" />
          {"Job " + this.props.match.params.jobId}
        </Modal.Header>
        <Modal.Content>
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
