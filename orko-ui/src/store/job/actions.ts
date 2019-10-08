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
import * as types from "./actionTypes"
import * as errorActions from "../error/actions"
import jobService from "../../services/job"
import * as jobTypes from "../../services/jobTypes"
import { AuthApi } from "modules/auth"
import { LogApi } from "modules/log"

export function submitJob(auth: AuthApi, job) {
  return auth.wrappedRequest(
    () => jobService.submitJob(job),
    null,
    error => errorActions.setForeground("Could not submit job: " + error.message),
    () => addJob(job)
  )
}

export function submitScriptJob(auth: AuthApi, job) {
  return auth.wrappedRequest(
    () => jobService.submitScriptJob(job),
    null,
    error => errorActions.setForeground("Could not submit job: " + error.message),
    () => addJob({ ...job, jobType: jobTypes.SCRIPT })
  )
}

function addJob(job) {
  return { type: types.ADD_JOB, payload: job }
}

export function fetchJobs(auth: AuthApi, logApi: LogApi) {
  return auth.wrappedRequest(
    () => jobService.fetchJobs(),
    jobs => ({ type: types.SET_JOBS, payload: jobs }),
    error => logApi.localMessage("Could not fetch jobs: " + error.message)
  )
}

export function deleteJob(auth: AuthApi, job) {
  return auth.wrappedRequest(
    () => jobService.deleteJob(job),
    null,
    error => errorActions.setForeground("Failed to delete job: " + error.message),
    () => ({ type: types.DELETE_JOB, payload: job })
  )
}
