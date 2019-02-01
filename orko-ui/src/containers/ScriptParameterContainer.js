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
