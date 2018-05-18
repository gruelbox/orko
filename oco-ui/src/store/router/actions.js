import * as types from './actionTypes';

export function locationChanged(location) {
  return { type: types.LOCATION_CHANGED, location }
}
