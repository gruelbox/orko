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
import React, { useState, useContext } from "react"
import PageVisibility from "react-page-visibility"
import RenderIf from "../components/RenderIf"

import * as jobUtils from "../util/jobUtils"

import Section from "../components/primitives/Section"
import Para from "../components/primitives/Para"
import Loading from "../components/primitives/Loading"
import JobShortComponent from "../components/JobShortComponent"
import Tab from "../components/primitives/Tab"
import { ServerContext, Job } from "modules/server"

enum Mode {
  ONLY_COMPLEX = "complexonly",
  ALL = "all"
}

const JobsContainer: React.FC<any> = () => {
  const serverApi = useContext(ServerContext)

  const [mode, setMode] = useState(Mode.ONLY_COMPLEX)
  const [visible, setVisible] = useState(true)

  const loading = serverApi.jobsLoading
  const complexOnly = mode === Mode.ONLY_COMPLEX
  const show = (job: Job) => !complexOnly || (!jobUtils.isAlert(job) && !jobUtils.isStop(job))
  const rawJobs = loading ? [] : serverApi.jobs.filter(job => show(job))

  const Content: React.FC<any> = () => {
    if (loading) {
      return <Loading />
    } else if (rawJobs.length === 0) {
      if (complexOnly) {
        return <Para>No complex jobs. </Para>
      } else {
        return <Para>No active jobs</Para>
      }
    } else {
      return (
        <>
          {rawJobs.map(job => (
            <JobShortComponent key={job.id} job={job} onRemove={() => serverApi.deleteJob(job.id)} />
          ))}
        </>
      )
    }
  }

  return (
    <PageVisibility onChange={(newState: boolean) => setVisible(newState)}>
      <RenderIf condition={visible}>
        <Section
          id="jobs"
          heading="Running Jobs"
          buttons={() => (
            <>
              <Tab
                data-orko={Mode.ONLY_COMPLEX}
                selected={mode === Mode.ONLY_COMPLEX}
                onClick={() => setMode(Mode.ONLY_COMPLEX)}
                title="Only show jobs which are not summarised elsewhere (such as alerts or simple server-side trades)"
              >
                Complex only
              </Tab>
              <Tab
                data-orko={Mode.ALL}
                selected={mode === Mode.ALL}
                onClick={() => setMode(Mode.ALL)}
                title="Show all jobs running in the background on the server"
              >
                All
              </Tab>
            </>
          )}
        >
          <Content />
        </Section>
      </RenderIf>
    </PageVisibility>
  )
}

export default JobsContainer
