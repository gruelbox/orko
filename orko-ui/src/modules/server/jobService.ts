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
import { put, get, del } from "modules/common/util/fetchUtil"
import { Job, ScriptJob } from "./Types"

class JobService {
  async submitJob(job: Job): Promise<Response> {
    return (await put("jobs/" + job.id, JSON.stringify(job))) as Promise<Response>
  }

  async submitScriptJob(job: ScriptJob): Promise<Response> {
    return (await put("scriptjobs/" + job.id, JSON.stringify(job))) as Promise<Response>
  }

  async deleteJob(id: string): Promise<Response> {
    return (await del("jobs/" + id)) as Promise<Response>
  }

  async fetchJobs(): Promise<Response> {
    return (await get("jobs")) as Promise<Response>
  }

  async fetchJob(jobId: string): Promise<Response> {
    return (await get("jobs/" + jobId)) as Promise<Response>
  }
}

export default new JobService()
