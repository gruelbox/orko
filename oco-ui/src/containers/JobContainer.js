import React from "react"
import { connect } from "react-redux"

import Job from "../components/Job"

import Section from "../components/primitives/Section"
import Para from "../components/primitives/Para"

class JobContainer extends React.Component {
  render() {
    const job = this.props.jobs.find(
      j => j.id === this.props.match.params.jobId
    )

    return (
      <Section
        id="job"
        bg="backgrounds.2"
        heading={"Job " + this.props.match.params.jobId}
      >
        {job && <Job job={job} />}
        {!job && <Para>Job {this.props.match.params.jobId} not found</Para>}
      </Section>
    )
  }
}

function mapStateToProps(state) {
  return {
    jobs: state.job.jobs
  }
}

export default connect(mapStateToProps)(JobContainer)
