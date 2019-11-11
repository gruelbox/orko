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
import React, { useContext, useMemo } from "react"
import Section, { Provider as SectionProvider } from "../components/primitives/Section"
import Window from "../components/primitives/Window"
import CreateAlertContainer from "./CreateAlertContainer"
import { isAlert } from "../util/jobUtils"
import Alerts from "components/Alerts"
import { FrameworkContext } from "FrameworkContainer"
import { ServerContext, OcoJob } from "modules/server"

interface ManageAlertsProps {
  mobile: boolean
}

const ManageAlertsContainer: React.FC<ManageAlertsProps> = props => {
  const frameworkApi = useContext(FrameworkContext)
  const serverApi = useContext(ServerContext)

  const coin = frameworkApi.alertsCoin
  const allJobs = serverApi.jobs
  const alerts = useMemo<OcoJob[]>(
    () =>
      !coin
        ? []
        : (allJobs
            .filter(job => isAlert(job))
            .map(job => job as OcoJob)
            .filter(
              job =>
                job.tickTrigger.exchange === coin.exchange &&
                job.tickTrigger.base === coin.base &&
                job.tickTrigger.counter === coin.counter
            ) as any).asMutable(),
    [allJobs, coin]
  )

  if (!coin) return null

  return (
    <Window mobile={props.mobile} large={false}>
      <SectionProvider
        value={{
          draggable: !props.mobile,
          onHide: () => frameworkApi.setAlertsCoin(null)
        }}
      >
        <Section id="manageAlerts" heading={"Manage alerts for " + coin.name}>
          <Alerts alerts={alerts} onDelete={job => serverApi.deleteJob(job.id)} />
          <CreateAlertContainer coin={coin} />
        </Section>
      </SectionProvider>
    </Window>
  )
}

export default ManageAlertsContainer
