import * as types from './actionTypes';

export function setUpdateAction(updateFunction) {
  return {
    type: types.SET_UPDATE_FUNCTION,
    payload: updateFunction
  }
}