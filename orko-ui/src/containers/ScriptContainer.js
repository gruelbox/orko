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

import Script from "../components/Script"
import ViewSource from "../components/ViewSource"

import { getSelectedCoin } from "../selectors/coins"

import uuidv4 from "uuid/v4"
import { withServer } from "modules/server"

class ScriptContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      name: "",
      script: undefined,
      parameters: {},
      viewSource: false
    }
  }

  onChangeName = name => {
    this.setState({
      name
    })
  }

  onChangeScript = script => {
    this.setState({
      name: script.name,
      script,
      parameters: script.parameters.reduce((o, p) => {
        o[p.name] = p.default ? p.default : ""
        return o
      }, {})
    })
  }

  onChangeParameters = parameters => {
    this.setState({
      parameters
    })
  }

  onViewSource = () => {
    this.setState({
      viewSource: true
    })
  }

  onCloseSource = () => {
    this.setState({
      viewSource: false
    })
  }

  createJob = () => {
    return {
      id: uuidv4(),
      name: this.state.name,
      script: this.generateScript()
    }
  }

  generateScript() {
    var script = "var parameters = {\n"

    script += "  selectedCoin: {\n"
    script += "    base: '" + this.props.coin.base + "',\n"
    script += "    counter: '" + this.props.coin.counter + "',\n"
    script += "    exchange: '" + this.props.coin.exchange + "',\n"
    script += "  },\n"

    for (var property in this.state.parameters) {
      if (this.state.parameters.hasOwnProperty(property)) {
        script += "  " + property + ": '" + this.state.parameters[property] + "',\n"
      }
    }

    script += "}\n"
    script += this.state.script.script

    return script
  }

  onSubmit = async () => {
    this.props.serverApi.submitScriptJob(this.createJob())
  }

  render() {
    return (
      <>
        <Script
          name={this.state.name}
          script={this.state.script}
          parameters={this.state.parameters}
          scripts={this.props.scripts}
          onChangeName={this.onChangeName}
          onChangeScript={this.onChangeScript}
          onChangeParameters={this.onChangeParameters}
          onViewSource={this.onViewSource}
          onSubmit={this.onSubmit}
        />
        {this.state.viewSource && <ViewSource script={this.state.script} onClose={this.onCloseSource} />}
      </>
    )
  }
}

function mapStateToProps(state) {
  return {
    auth: state.auth,
    scripts: state.scripting.scripts,
    coin: getSelectedCoin(state)
  }
}

export default withServer(connect(mapStateToProps)(ScriptContainer))
