import React from "react"

import Input from "./primitives/Input"
import Form from "./primitives/Form"
import Button from "./primitives/Button"

const Script = ({
  name,
  script,
  scripts,
  parameters,
  onSubmit,
  onChangeName,
  onChangeScript,
  onChangeParameters
}) => {
  const scriptValid = !!script
  const nameValid = name && name !== ""
  const parametersValid =
    script && script.parameters.every(p => parameters[p.name] !== "")
  const valid = scriptValid && nameValid && parametersValid
  return (
    <Form
      data-orko="script"
      buttons={() => (
        <>
          <Button
            data-orko="submitScript"
            disabled={!valid}
            onClick={onSubmit}
            width={120}
          >
            Submit
          </Button>
        </>
      )}
    >
      <Input
        id="type"
        label="Select script"
        type="select"
        value={script ? script.id : undefined}
        error={!scriptValid}
        onChange={e =>
          onChangeScript(scripts.find(s => s.id === e.target.value))
        }
        options={scripts.map(s => ({ value: s.id, name: s.name }))}
      />
      <Input
        id="name"
        label="Job name"
        placeholder="Enter name..."
        error={!nameValid}
        value={name}
        onChange={e => onChangeName(e.target.value)}
      />
      {script &&
        script.parameters.map(p => (
          <Input
            key={"parameter-" + p.name}
            id={"parameter-" + p.name}
            label={p.description}
            error={parameters[p.name] === "" && p.mandatory}
            placeholder={"Enter " + p.description + "..."}
            value={parameters[p.name]}
            onChange={e =>
              onChangeParameters({ ...parameters, [p.name]: e.target.value })
            }
          />
        ))}
    </Form>
  )
}

export default Script
