import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  jobs: Immutable([]),
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.ADD_JOB:
      return Immutable.merge(state, {
        jobs: state.jobs.concat([action.payload]),
      });
    case types.DELETE_JOB:
      return Immutable.merge(state, {
        jobs: state.jobs.filter(j => j.id !== action.payload.id),
      });
    case types.SET_JOBS:
      return Immutable.merge(state, {
        jobs: action.payload,
      });
    default:
      return state;
  }
}