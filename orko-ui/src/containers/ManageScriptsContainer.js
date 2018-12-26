import React from "react"
import { connect } from "react-redux"
import Confirmation from "../components/Confirmation"
import ScriptManagement from "../components/ScriptManagement"
import Scripts from "../components/Scripts"
import ScriptEditor from "../components/ScriptEditor"
import ScriptParameterContainer from "./ScriptParameterContainer"
import { newScript, newParameter } from "../store/scripting/reducer"
import * as scriptActions from "../store/scripting/actions"
import { replaceInArray } from "../util/objectUtils"

import uuidv4 from "uuid/v4"

const NEW_SCRIPT_ROUTE = "new"

class ManageScriptsContainer extends React.Component {
  constructor(props) {
    super(props)

    var scriptState

    if (
      props.scripts.length === 0 ||
      this.props.match.params.id === NEW_SCRIPT_ROUTE
    ) {
      scriptState = newScript
    } else {
      if (this.props.match.params.id) {
        scriptState = props.scripts.find(
          s => s.id === this.props.match.params.id
        )
      } else {
        scriptState = props.scripts[0]
      }
    }

    this.state = {
      current: {
        ...scriptState
      },
      modified: false
    }
  }

  selectedScriptId = () => this.props.match.params.id

  isNewScript = () => this.selectedScriptId() === NEW_SCRIPT_ROUTE

  changeScript = id => {
    if (this.state.modified) {
      this.setState({
        check: {
          message: "Unsaved changes. Are you sure?",
          onOk: () => this.props.history.push("/scripts/" + id)
        }
      })
    } else {
      this.props.history.push("/scripts/" + id)
    }
  }

  onSelect = script => {
    this.changeScript(script.id)
  }

  onNew = () => {
    this.changeScript(NEW_SCRIPT_ROUTE)
  }

  onDelete = () => {
    var nextChoice = this.props.scripts.find(
      script => script.id !== this.selectedScriptId()
    )
    this.props.dispatch(scriptActions.remove(this.selectedScriptId()))
    if (nextChoice) {
      this.onSelect(nextChoice)
    } else {
      this.onNew()
    }
  }

  onSave = () => {
    if (this.isNewScript()) {
      this.setState(
        state => ({
          current: { ...state.current, id: uuidv4() },
          modified: false
        }),
        () => {
          this.props.dispatch(scriptActions.add(this.state.current))
          this.props.history.push("/scripts/" + this.state.current.id)
        }
      )
    } else {
      this.setState({ modified: false }, () =>
        this.props.dispatch(scriptActions.update(this.state.current))
      )
    }
  }

  onChangeState = state => {
    this.setState({ current: state, modified: true })
  }

  onEditParameter = parameter => {
    this.setState({ editingParameter: true, parameterUnderEdit: parameter })
  }

  onAddParameter = () => {
    this.setState({ editingParameter: true, parameterUnderEdit: newParameter })
  }

  onCloseParameterEditor = () => {
    this.setState({ editingParameter: false, parameterUnderEdit: undefined })
  }

  onDeleteParameter = () => {
    var parameter = this.state.parameterUnderEdit
    this.setState(state => ({
      editingParameter: false,
      parameterUnderEdit: undefined,
      current: {
        ...state.current,
        parameters: state.current.parameters.filter(
          p => p.name !== parameter.name
        )
      }
    }))
  }

  onUpdateParameter = parameter => {
    this.setState(state => ({
      editingParameter: false,
      parameterUnderEdit: undefined,
      current: {
        ...state.current,
        parameters: replaceInArray(
          state.current.parameters,
          parameter,
          p => p.name === parameter.name
        )
      }
    }))
  }

  render() {
    return (
      <>
        <ScriptManagement
          onClose={() => this.props.history.push("/")}
          onDelete={this.onDelete}
          onNew={this.onNew}
          onSave={this.onSave}
          deleteEnabled={!this.isNewScript()}
          listing={
            <Scripts
              scripts={this.props.scripts}
              onSelect={this.onSelect}
              modified={this.state.modified}
              selected={this.state.current.id}
            />
          }
          editor={
            <ScriptEditor
              state={this.state.current}
              onChangeState={this.onChangeState}
              onAddParameter={this.onAddParameter}
              onEditParameter={this.onEditParameter}
            />
          }
        />
        {this.state.check && (
          <Confirmation
            message={this.state.check.message}
            onCancel={() => this.setState({ check: undefined })}
            onOk={() => {
              const onOk = this.state.check.onOk
              this.setState({ check: undefined }, onOk)
            }}
          />
        )}
        {this.state.editingParameter && (
          <ScriptParameterContainer
            parameter={this.state.parameterUnderEdit}
            onDelete={() =>
              this.onDeleteParameter(this.state.parameterUnderEdit)
            }
            onUpdate={updated => this.onUpdateParameter(updated)}
            onClose={this.onCloseParameterEditor}
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
