import * as types from './actionTypes';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';
import jobService from '../../services/job';
import * as jobTypes from '../../services/jobTypes'
import * as notificationActions from "../notifications/actions"

export function submitJob(job) {
  return authActions.wrappedRequest(
    auth => jobService.submitJob(job, auth.token),
    null,
    error => errorActions.setForeground("Could not submit job: " + error.message),
    () => ({ type: types.ADD_JOB, payload: job })
  );
}

export function submitWatchJob(coin, orderId) {
  return submitJob({
    jobType: jobTypes.WATCH_JOB,
    tickTrigger: {
      exchange: coin.exchange,
      base: coin.base,
      counter: coin.counter
    },
    orderId
  })
}

export function fetchJobs() {
  return authActions.wrappedRequest(
    auth => jobService.fetchJobs(auth.token),
    jobs => ({ type: types.SET_JOBS, payload: jobs }),
    error => notificationActions.localError("Could not fetch jobs: " + error.message)
  );
}

export function deleteJob(job) {
  return authActions.wrappedRequest(
    auth => jobService.deleteJob(job, auth.token),
    null,
    error => notificationActions.localError("Failed to delete job: " + error.message),
    () => ({ type: types.DELETE_JOB, payload: job })
  );
}