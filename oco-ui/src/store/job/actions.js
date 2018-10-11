import * as types from "./actionTypes"
import * as authActions from "../auth/actions"
import * as errorActions from "../error/actions"
import jobService from "../../services/job"
import * as notificationActions from "../notifications/actions"

export function submitJob(job, callback) {
  return authActions.wrappedRequest(
    auth => jobService.submitJob(job, auth.token),
    job => addJob(job, callback),
    error =>
      errorActions.setForeground("Could not submit job: " + error.message)
  )
}

function addJob(job, callback) {
  return async (dispatch, getState) => {
    dispatch(notificationActions.addStatusCallback(job.id, callback))
    dispatch({ type: types.ADD_JOB, payload: job })
  }
}

export function fetchJobs() {
  return authActions.wrappedRequest(
    auth => jobService.fetchJobs(auth.token),
    jobs => ({ type: types.SET_JOBS, payload: jobs }),
    error =>
      notificationActions.localMessage("Could not fetch jobs: " + error.message)
  )
}

export function deleteJob(job) {
  return authActions.wrappedRequest(
    auth => jobService.deleteJob(job, auth.token),
    null,
    error =>
      errorActions.setForeground("Failed to delete job: " + error.message),
    () => ({ type: types.DELETE_JOB, payload: job })
  )
}
