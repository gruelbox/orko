import React from "react"
import ScriptParameterEditor from "../components/ScriptParameterEditor"

export default class ScriptParameterContainer extends React.Component {
  state = {
    current: { ...this.props.parameter }
  }

  validate() {
    return (
      this.state.current.name !== "" && this.state.current.description !== ""
    )
  }

  render() {
    return (
      <ScriptParameterEditor
        parameter={this.state.current}
        existing={this.props.parameter.name !== ""}
        onChange={newState => this.setState({ current: { ...newState } })}
        onDelete={this.props.onDelete}
        onUpdate={() => this.props.onUpdate(this.state.current)}
        onClose={this.props.onClose}
        valid={this.validate()}
      />
    )
  }
}
