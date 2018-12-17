import React from "react"
import { connect } from "react-redux"
import Section from "../components/primitives/Section"
import Modal from "../components/primitives/Modal"
import Href from "../components/primitives/Href"
import * as uiActions from "../store/ui/actions"
import { Icon } from "semantic-ui-react"
import Scripts from "../components/Scripts"
import ScriptEditor from "../components/ScriptEditor"

class ManageScriptsContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      visible: false
    }
  }

  selectScript = script => {
    this.setState({
      ...script,
      visible: true
    })
  }

  render() {
    if (!this.props.visible) return null
    return (
      <Modal mobile={this.props.mobile} large>
        <Section
          id="manageScripts"
          heading={"Manage scripts"}
          nopadding
          buttons={() => (
            <Href
              data-orko="close"
              title="Close"
              onClick={() => this.props.dispatch(uiActions.closeScripts())}
            >
              <Icon fitted name="close" />
            </Href>
          )}
        >
          <div
            style={{
              display: "flex",
              height: "100%"
            }}
          >
            <div
              style={{
                flex: "20%",
                height: "100%",
                borderRight: "1px solid rgba(0,0,0,0.4)",
                boxShadow: "0 2px 15px 0 rgba(0, 0, 0, 0.15)"
              }}
            >
              <Scripts
                scripts={this.props.scripts}
                onSelect={script => this.selectScript(script)}
              />
            </div>
            <div
              style={{
                flex: "80%",
                padding: "10px"
              }}
            >
              {this.state.visible && (
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
              )}
            </div>
          </div>
        </Section>
      </Modal>
    )
  }
}

function mapStateToProps(state) {
  return {
    visible: state.ui.showScripts,
    scripts: state.scripting.scripts
  }
}

export default connect(mapStateToProps)(ManageScriptsContainer)
