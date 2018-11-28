import React from "react"
import { connect } from "react-redux"
import PageVisibility from "react-page-visibility"
import RenderIf from "../components/RenderIf"

import * as jobActions from "../store/job/actions"
import * as jobUtils from "../util/jobUtils"

import Section from "../components/primitives/Section"
import Para from "../components/primitives/Para"
import Loading from "../components/primitives/Loading"
import JobShort from "../components/JobShort"
import Tab from "../components/primitives/Tab"

class JobsContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      loading: true,
      selected: "onlycomplex",
      visible: true
    }
  }

  handleVisibilityChange = visible => {
    this.setState({ visible })
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.jobs) this.setState({ loading: false })
  }

  onRemove = job => {
    this.props.dispatch(jobActions.deleteJob(job))
  }

  render() {
    const onRemove = this.onRemove
    const complexOnly = this.state.selected === "onlycomplex"
    const show = job =>
      !complexOnly || (!jobUtils.isAlert(job) && !jobUtils.isStop(job))
    const rawJobs = this.props.jobs.filter(job => show(job))
    var jobs
    if (this.state.loading) {
      jobs = <Loading />
    } else if (rawJobs.length === 0) {
      if (complexOnly) {
        jobs = <Para>No complex jobs. </Para>
      } else {
        jobs = <Para>No active jobs</Para>
      }
    } else {
      jobs = rawJobs.map(job => (
        <JobShort key={job.id} job={job} onRemove={() => onRemove(job)} />
      ))
    }

    var buttons = (
      <span>
        <Tab
          data-orko="onlycomplex"
          selected={this.state.selected === "onlycomplex"}
          onClick={() => this.setState({ selected: "onlycomplex" })}
        >
          Complex only
        </Tab>
        <Tab
          data-orko="all"
          selected={this.state.selected === "all"}
          onClick={() => this.setState({ selected: "all" })}
        >
          All
        </Tab>
      </span>
    )

    return (
      <PageVisibility onChange={this.handleVisibilityChange}>
        <RenderIf condition={this.state.visible}>
          <Section id="jobs" heading="Running Jobs" buttons={() => buttons}>
            {jobs}
          </Section>
        </RenderIf>
      </PageVisibility>
    )
  }
}

function mapStateToProps(state) {
  return {
    jobs: state.job.jobs
  }
}

export default connect(mapStateToProps)(JobsContainer)
