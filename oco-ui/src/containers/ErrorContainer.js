import React from 'react';
import { connect } from 'react-redux';
import { Button, Header, Icon, Modal } from 'semantic-ui-react'
import FixedModal from '../components/primitives/FixedModal'

import * as errorActions from '../store/error/actions';

const ErrorContainer = props => {
  if (props.error !== null) {
    return (
      <FixedModal defaultOpen>
        <Header icon='warning' content='Error' />
        <Modal.Content>
          <p>{props.error}</p>
        </Modal.Content>
        <Modal.Actions>
          <Button
            onClick={() => props.dispatch(errorActions.clearError())}
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
    error: state.error.error
  };
}

export default connect(mapStateToProps)(ErrorContainer);