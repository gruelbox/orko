import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  jobs: Immutable([]),
  error: null
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_JOBS:
      console.log(action.type, action);
      return Immutable.merge(state, {
        jobs: Immutable(action.jobs),
        error: null,
      });
    case types.SET_JOBS_FAILED:
      console.log(action.type, action);
      return Immutable.merge(state, {
        jobs: Immutable([]),
        error: state.loading ? null : "Error fetching jobs: " + action.error,
      });
    default:
      return state;
  }
}