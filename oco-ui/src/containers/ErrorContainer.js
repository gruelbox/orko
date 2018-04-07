import React from 'react';
import { connect } from 'react-redux';
import { Button, Header, Modal } from 'semantic-ui-react'
import FixedModal from '../components/primitives/FixedModal'

import * as errorActions from '../store/error/actions';

const ErrorContainer = props => {
  if (props.errorForeground !== null) {
    return (
      <FixedModal defaultOpen>
        <Header icon='warning' content='Error' />
        <Modal.Content>
          <p>{props.errorForeground}</p>
        </Modal.Content>
        <Modal.Actions>
          <Button
            onClick={() => props.dispatch(errorActions.clearForeground())}
          >
            OK
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  } else {
    return null;
  }
};

function mapStateToProps(state) {
  return {
    errorForeground: state.error.errorForeground
  };
}

export default connect(mapStateToProps)(ErrorContainer);