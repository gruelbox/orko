import * as types from './actionTypes';

export function update(changes) {
  return {
    type: types.UPDATE,
    changes
  };
}

export function updateProperty(name, value) {
  return {
    type: types.UPDATE_PROPERTY,
    name,
    value
  };
}