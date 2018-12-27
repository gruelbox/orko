import React from "react"
import FixedModal from "./primitives/FixedModal"
import { Modal, Icon, Grid, Button, Loader, Dimmer } from "semantic-ui-react"

const ScriptManagement = ({
  onClose,
  onDelete,
  onNew,
  onSave,
  deleteEnabled,
  saveEnabled,
  listing,
  editor,
  loading
}) => (
  <FixedModal
    data-orko="manageScripts"
    closeIcon
    size="fullscreen"
    onClose={onClose}
    style={{ height: "100%" }}
  >
    <Modal.Header>
      <Icon name="code" />
      Manage scripts
    </Modal.Header>
    <Dimmer.Dimmable as={Modal.Content} dimmed={loading}>
      <Dimmer active={loading}>
        <Loader />
      </Dimmer>
      <Grid columns="2" divided style={{ height: "75vh" }}>
        <Grid.Row style={{ height: "100%" }}>
          <Grid.Column width={4}>{listing}</Grid.Column>
          <Grid.Column width="twelve">{editor}</Grid.Column>
        </Grid.Row>
      </Grid>
    </Dimmer.Dimmable>
    <Modal.Actions>
      <Button
        floated="left"
        negative
        data-orko="delete"
        disabled={!deleteEnabled}
        onClick={onDelete}
        title="Delete the selected script"
      >
        Delete
      </Button>
      <Button
        color="green"
        data-orko="verify"
        onClick={onNew}
        title="Create a new script"
      >
        New
      </Button>
      <Button
        primary
        disabled={!saveEnabled}
        data-orko="save"
        title="Save the script"
        onClick={onSave}
      >
        Save
      </Button>
    </Modal.Actions>
  </FixedModal>
)
export default ScriptManagement
