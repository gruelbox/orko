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
          required
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
          required
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
              required={p.mandatory}
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
