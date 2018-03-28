import * as types from './actionTypes';

export function setUpdateAction(updateAction) {
  return {
    type: types.SET_UPDATE_ACTION,
    payload: updateAction
  }
}