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
import * as types from "./actionTypes"
import * as authActions from "../auth/actions"
import * as errorActions from "../error/actions"
import jobService from "../../services/job"
import * as notificationActions from "../notifications/actions"
import * as jobTypes from "../../services/jobTypes"

export function submitJob(job, callback) {
  return authActions.wrappedRequest(
    auth => jobService.submitJob(job),
    null,
    error =>
      errorActions.setForeground("Could not submit job: " + error.message),
    () => addJob(job, callback)
  )
}

export function submitScriptJob(job, callback) {
  return authActions.wrappedRequest(
    auth => jobService.submitScriptJob(job),
    null,
    error =>
      errorActions.setForeground("Could not submit job: " + error.message),
    () => addJob({...job, jobType: jobTypes.SCRIPT}, callback)
  )
}

function addJob(job, callback) {
  return async (dispatch, getState) => {
    if (callback)
      dispatch(notificationActions.addStatusCallback(job.id, callback))
    dispatch({ type: types.ADD_JOB, payload: job })
  }
}

export function fetchJobs() {
  return authActions.wrappedRequest(
    auth => jobService.fetchJobs(),
    jobs => ({ type: types.SET_JOBS, payload: jobs }),
    error =>
      notificationActions.localMessage("Could not fetch jobs: " + error.message)
  )
}

export function deleteJob(job) {
  return authActions.wrappedRequest(
    auth => jobService.deleteJob(job),
    null,
    error =>
      errorActions.setForeground("Failed to delete job: " + error.message),
    () => ({ type: types.DELETE_JOB, payload: job })
  )
}
