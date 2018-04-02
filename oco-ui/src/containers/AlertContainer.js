import React from 'react';
import { connect } from 'react-redux';
import * as alertActions from '../store/alert/actions';
import * as focusActions from '../store/focus/actions';
import Alert from '../components/Alert';

const AlertContainer = props => (
  <Alert
    job={props.alert}
    onChange={job => props.dispatch(alertActions.update(job))}
    onFocus={focusedProperty =>
      props.dispatch(
        focusActions.setUpdateAction(value =>
          alertActions.updateProperty(focusedProperty, value)
        )
      )
    }
  />
);

function mapStateToProps(state) {
  return {
    alert: state.alert
  };
}

export default connect(mapStateToProps)(AlertContainer);