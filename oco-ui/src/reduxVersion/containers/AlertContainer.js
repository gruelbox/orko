import React from 'react';
import { connect } from 'react-redux';
import * as alertActions from '../store/alert/actions';
import * as focusActions from '../store/focus/actions';
import AlertComponent from '../components/AlertComponent';

const AlertContainer = props => (
  <AlertComponent
    job={props.alert}
    onChange={job => props.dispatch(alertActions.update(job))}
    onFocus={focus => props.dispatch(focusActions.setUpdateAction(value => alertActions.updateProperty(focus, value)))}
  />
);

function mapStateToProps(state) {
  return {
    alert: state.alert.job
  };
}

export default connect(mapStateToProps)(AlertContainer);