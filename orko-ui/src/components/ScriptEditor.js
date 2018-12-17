import React from "react"
import Input from "./primitives/Input"
import Form from "./primitives/Form"
import Button from "./primitives/Button"
import SimpleCodeEditor from "react-simple-code-editor"
import { highlight, languages } from "prismjs/components/prism-core"
import "prismjs/components/prism-clike"
import "prismjs/components/prism-javascript"
import theme from "../theme"

const ScriptEditor = ({
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

export default ScriptEditor
