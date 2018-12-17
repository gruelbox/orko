import React from "react"
import { connect } from "react-redux"
import Section from "../components/primitives/Section"
import Modal from "../components/primitives/Modal"
import Href from "../components/primitives/Href"
import * as uiActions from "../store/ui/actions"
import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"
import Input from "../components/primitives/Input"
import Form from "../components/primitives/Form"
import Button from "../components/primitives/Button"
import SimpleCodeEditor from "react-simple-code-editor"
import { highlight, languages } from "prismjs/components/prism-core"
import "prismjs/components/prism-clike"
import "prismjs/components/prism-javascript"
import theme from "../theme"

const textStyle = {
  textAlign: "left"
}

const Scripts = ({ scripts, onDelete, onSelect }) => (
  <ReactTable
    data={scripts}
    columns={[
      {
        id: "close",
        Header: null,
        Cell: ({ original }) => (
          <Href title="Remove script" onClick={() => onDelete(original)}>
            <Icon fitted name="close" />
          </Href>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 32,
        sortable: false,
        resizable: false
      },
      {
        id: "name",
        Header: "name",
        Cell: ({ original }) => (
          <Href
            data-orko={original.id + "/select"}
            title={"Select " + original.name}
            onClick={() => onSelect(original)}
          >
            {original.name}
          </Href>
        ),
        headerStyle: textStyle,
        style: textStyle,
        resizable: true,
        minWidth: 50
      }
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No scripts"
  />
)

const Editor = ({
  name,
  script,
  parameters,
  onChangeName,
  onChangeScript,
  onChangeParameters
}) => (
  <Form
    flex-direction="column"
    buttons={() => (
      <>
        <Button data-orko="verify">Verify</Button>
        <Button data-orko="save">Save</Button>
      </>
    )}
  >
    <Input
      id="name"
      label="Name"
      placeholder="Enter name..."
      value={name}
      onChange={e => onChangeName && onChangeName(e.target.value)}
      width="100%"
      mr={0}
    />
    <Input
      id="parameters"
      label="Parameters"
      placeholder="Enter comma-separated list of parameters..."
      value={parameters}
      onChange={e => onChangeParameters && onChangeName(e.target.value)}
      width="100%"
      mr={0}
    />
    <SimpleCodeEditor
      value={script}
      onValueChange={code => onChangeScript && onChangeScript(code)}
      highlight={code => (code ? highlight(code, languages.js) : "")}
      padding={theme.space[2]}
      style={{
        fontFamily: '"Fira code", "Fira Mono", monospace',
        fontSize: 12,
        border: "2px solid " + theme.colors.inputBg,
        borderRadius: theme.radii[2] + "px",
        backgroundColor: theme.colors.inputBg,
        width: "100%",
        flex: "1",
        overflow: "scroll"
      }}
    />
  </Form>
)

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
                <Editor
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
