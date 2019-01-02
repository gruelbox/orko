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
