import Immutable from 'seamless-immutable';
import * as types from './actionTypes';

const initialState = Immutable({
  jobs: Immutable([]),
});

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.ADD_JOB:
      return Immutable.merge(state, {
        jobs: state.jobs.concat([action.job]),
      });
    case types.SET_JOBS:
      return Immutable.merge(state, {
        jobs: Immutable(action.jobs),
      });
    default:
      return state;
  }
}