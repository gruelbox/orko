import React from "react"

import FormButtonBar from "./primitives/FormButtonBar"
import RawForm from "./primitives/RawForm"
import { Form, Icon } from "semantic-ui-react"

const Script = ({
  name,
  script,
  scripts,
  parameters,
  onSubmit,
  onChangeName,
  onChangeScript,
  onChangeParameters,
  onViewSource
}) => {
  const scriptValid = !!script
  const nameValid = name && name !== ""
  const parametersValid =
    script && script.parameters.every(p => parameters[p.name] !== "")
  const valid = scriptValid && nameValid && parametersValid
  return (
    <Form data-orko="script" as={RawForm}>
      <Form.Group>
        <Form.Dropdown
          id="type"
          label="Select script"
          search
          value={script ? script.id : undefined}
          selection
          onChange={(e, { value }) =>
            onChangeScript(scripts.find(s => s.id === value))
          }
          options={scripts.map(s => ({ value: s.id, text: s.name }))}
        />
        <Form.Input
          id="name"
          label="Job name"
          placeholder="Enter name..."
          value={name}
          onChange={e => onChangeName(e.target.value)}
        />
      </Form.Group>
      <Form.Group style={{ flex: "1" }}>
        {script &&
          script.parameters.map(p => (
            <Form.Input
              key={"parameter-" + p.name}
              id={"parameter-" + p.name}
              label={p.description}
              placeholder={"Enter " + p.description + "..."}
              value={parameters[p.name]}
              onChange={e =>
                onChangeParameters({ ...parameters, [p.name]: e.target.value })
              }
            />
          ))}
      </Form.Group>
      <FormButtonBar>
        <Form.Button
          disabled={!script}
          secondary
          icon
          onClick={onViewSource}
          title="Show source"
        >
          <Icon name="code" />
        </Form.Button>
        <Form.Button
          data-orko="submitScript"
          disabled={!valid}
          onClick={onSubmit}
        >
          Submit
        </Form.Button>
      </FormButtonBar>
    </Form>
  )
}

export default Script
