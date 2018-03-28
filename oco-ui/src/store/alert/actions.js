import * as types from './actionTypes';

export function update(changes) {
  return {
    type: types.UPDATE,
    payload: changes
  };
}

export function updateProperty(name, value) {
  return {
    type: types.UPDATE,
    payload: {
      [name]: value
    }
  };
}