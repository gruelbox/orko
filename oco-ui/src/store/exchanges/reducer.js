import Immutable from 'seamless-immutable';
import PropTypes from 'prop-types';
import * as types from './actionTypes';

const initialState = Immutable({
});

export const shape = {
};

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    default:
      return state;
  }
}