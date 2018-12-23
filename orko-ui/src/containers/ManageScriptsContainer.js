import React from "react"
import { connect } from "react-redux"
import FixedModal from "../components/primitives/FixedModal"
import { Modal, Icon, Grid, Button } from "semantic-ui-react"
import Scripts from "../components/Scripts"
import Confirmation from "../components/Confirmation"
import ScriptEditor from "../components/ScriptEditor"
import { newScript } from "../store/scripting/reducer"
import * as scriptActions from "../store/scripting/actions"

import uuidv4 from "uuid/v4"

const newScriptState = {
  ...newScript,
  id: undefined,
  modified: true,
  check: undefined
}

const selectScriptState = script => ({
  ...script,
  modified: false,
  check: undefined
})

class ManageScriptsContainer extends React.Component {
  constructor(props) {
    super(props)
    if (props.scripts.length === 0) {
      this.state = newScriptState
    } else {
      this.state = selectScriptState(props.scripts[0])
    }
  }

  changeScript = targetState => {
    this.setState(state => {
      if (state.modified) {
        return {
          check: {
            message: "Unsaved changes. Are you sure?",
            onOk: () => {
              this.setState(targetState)
            }
          }
        }
      } else {
        return targetState
      }
    })
  }

  onSelectScript = script => {
    this.changeScript(selectScriptState(script))
  }

  onNew = () => {
    this.changeScript(newScriptState)
  }

  onDelete = () => {
    var nextChoice = this.props.scripts.find(
      script => script.id !== this.state.id
    )
    if (nextChoice) {
      this.onSelectScript(nextChoice)
    } else {
      this.onNew()
    }
    this.props.dispatch(scriptActions.remove(this.state.id))
  }

  onSave = () => {
    if (this.state.id === undefined) {
      this.setState({ id: uuidv4(), modified: false }, () =>
        this.props.dispatch(scriptActions.add(this.state))
      )
    } else {
      this.props.dispatch(scriptActions.update(this.state))
    }
  }

  render() {
    return (
      <>
        <FixedModal
          data-orko="manageScripts"
          closeIcon
          size="fullscreen"
          onClose={() => this.props.history.goBack()}
          style={{ height: "100%" }}
        >
          <Modal.Header>
            <Icon name="code" />
            Manage scripts
          </Modal.Header>
          <Modal.Content>
            <Grid columns="2" divided style={{ height: "75vh" }}>
              <Grid.Row style={{ height: "100%" }}>
                <Grid.Column width={4}>
                  <Scripts
                    scripts={this.props.scripts}
                    onSelect={script => this.onSelectScript(script)}
                    modified={this.state.modified}
                    selected={{
                      id: this.state.id,
                      name: this.state.name
                    }}
                  />
                </Grid.Column>
                <Grid.Column width="twelve">
                  <ScriptEditor
                    name={this.state.name}
                    script={this.state.script}
                    parameters={this.state.parameters}
                    onChangeName={name => this.setState({ name })}
                    onChangeScript={script => this.setState({ script })}
                    onChangeParameters={parameters =>
                      this.setState({ parameters })
                    }
                  />
                </Grid.Column>
              </Grid.Row>
            </Grid>
          </Modal.Content>
          <Modal.Actions>
            <Button
              floated="left"
              negative
              data-orko="delete"
              disabled={this.state.id === undefined}
              onClick={this.onDelete}
              title="Delete the selected script"
            >
              Delete
            </Button>
            <Button
              color="green"
              data-orko="verify"
              onClick={this.onNew}
              title="Create a new script"
            >
              New
            </Button>
            <Button
              primary
              data-orko="save"
              title="Save the script"
              onClick={this.onSave}
            >
              Save
            </Button>
          </Modal.Actions>
        </FixedModal>

        {this.state.check && (
          <Confirmation
            message={this.state.check.message}
            onCancel={() => this.setState({ check: undefined })}
            onOk={this.state.check.onOk}
          />
        )}
      </>
    )
  }
}

function mapStateToProps(state) {
  return {
    scripts: state.scripting.scripts
  }
}

export default connect(mapStateToProps)(ManageScriptsContainer)
