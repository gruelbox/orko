/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React from "react"
import { connect } from "react-redux"
import Confirmation from "../components/Confirmation"
import ScriptManagement from "../components/ScriptManagement"
import Scripts from "../components/Scripts"
import ScriptEditor from "../components/ScriptEditor"
import ScriptParameterContainer from "./ScriptParameterContainer"
import { newScript, newParameter } from "../store/scripting/reducer"
import * as scriptActions from "../store/scripting/actions"
import { replaceInArray } from "modules/common/util/objectUtils"

import uuidv4 from "uuid/v4"
import { withAuth } from "modules/auth"

const NEW_SCRIPT_ROUTE = "new"

class ManageScriptsContainerOuter extends React.Component {
  componentDidMount() {
    this.props.dispatch(scriptActions.fetch(this.props.auth))
  }

  render() {
    return (
      <ManageScriptsContainerInner
        key={this.props.loading + "-" + this.props.match.params.id}
        scripts={this.props.scripts}
        loading={this.props.loading}
        match={this.props.match}
        history={this.props.history}
        dispatch={this.props.dispatch}
        auth={this.props.auth}
      />
    )
  }
}

class ManageScriptsContainerInner extends React.Component {
  constructor(props) {
    super(props)

    var scriptState = null

    if (props.match.params.id === NEW_SCRIPT_ROUTE) {
      scriptState = { ...newScript }
    } else if (props.match.params.id) {
      var found = props.scripts.find(s => s.id === props.match.params.id)
      if (found) scriptState = { ...found }
    }

    this.state = {
      current: scriptState,
      modified: false
    }
  }

  selectedScriptId = () => this.props.match.params.id

  isNewScript = () => this.state.current && !this.state.current.id

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
    var nextChoice = this.props.scripts.find(script => script.id !== this.selectedScriptId())
    this.props.dispatch(scriptActions.remove(this.props.auth, this.selectedScriptId()))
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
          this.props.dispatch(scriptActions.add(this.props.auth, this.state.current))
          this.props.history.push("/scripts/" + this.state.current.id)
        }
      )
    } else {
      this.setState({ modified: false }, () =>
        this.props.dispatch(scriptActions.update(this.props.auth, this.state.current))
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
        parameters: state.current.parameters.filter(p => p.name !== parameter.name)
      }
    }))
  }

  onUpdateParameter = parameter => {
    this.setState(state => ({
      editingParameter: false,
      parameterUnderEdit: undefined,
      current: {
        ...state.current,
        parameters: replaceInArray(state.current.parameters, parameter, p => p.name === parameter.name)
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
          deleteEnabled={!!this.state.current && !this.isNewScript()}
          saveEnabled={!!this.state.current}
          listing={
            <Scripts
              scripts={this.props.scripts}
              onSelect={this.onSelect}
              modified={this.state.modified}
              selected={!!this.state.current && this.state.current.id}
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
          loading={this.props.loading}
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
            onDelete={() => this.onDeleteParameter(this.state.parameterUnderEdit)}
            onUpdate={updated => this.onUpdateParameter(updated)}
            onClose={this.onCloseParameterEditor}
          />
        )}
      </>
    )
  }
}

export default withAuth(
  connect(state => ({
    scripts: state.scripting.scripts,
    loading: !state.scripting.loaded
  }))(ManageScriptsContainerOuter)
)
