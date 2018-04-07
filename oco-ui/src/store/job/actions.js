import * as types from './actionTypes';
import * as authActions from '../auth/actions';
import * as errorActions from '../error/actions';
import jobService from '../../services/job';

export function submitJob(job) {
  return async(dispatch, getState) => {
    try {
      const response = await jobService.submitJob(job, getState().auth.token);
      if (!response.ok) {
        const authAction = authActions.handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      }
      dispatch({ type: types.ADD_JOB, job });
    } catch (error) {
      dispatch(errorActions.setError(error.message));
    }
  };
}

export function fetchJobs() {
  return async(dispatch, getState) => {
    try {
      const response = await jobService.fetchJobs(getState().auth.token);
      if (!response.ok) {
        const authAction = authActions.handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      }
      const jobs = await response.json();
      dispatch({ type: types.SET_JOBS, jobs });
    } catch (error) {
      dispatch(errorActions.setError(error.message));
    }
  };
}

export function deleteJob(job) {
  return async(dispatch, getState) => {
    try {
      const response = await jobService.deleteJob(job, getState().auth.token);
      if (!response.ok) {
        const authAction = authActions.handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      }
      dispatch(fetchJobs());
    } catch (error) {
      dispatch(errorActions.setError(error.message));
    }
  };
}