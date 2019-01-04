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
import { Form, Grid, Table, Icon, Button } from "semantic-ui-react"

const ScriptEditor = ({
  state,
  onChangeState,
  onAddParameter,
  onEditParameter
}) =>
  !!state && (
    <Form
      style={{
        height: "100%"
      }}
    >
      <Grid columns="2" style={{ height: "100%" }}>
        <Grid.Row style={{ height: "100%" }}>
          <Grid.Column
            width={10}
            style={{ height: "100%", display: "flex", flexDirection: "column" }}
          >
            <Form.Input
              required
              id="name"
              label="Name"
              placeholder="Enter name..."
              error={state.name === ""}
              value={state.name}
              onChange={e => onChangeState({ ...state, name: e.target.value })}
            />
            <Form.Field
              required
              error={state.script === ""}
              style={{
                flex: "2",
                display: "flex",
                flexDirection: "column"
              }}
            >
              <label>Code</label>
              <textarea
                value={state.script}
                onChange={e =>
                  onChangeState({ ...state, script: e.target.value })
                }
                style={{
                  fontFamily: '"Fira code", "Fira Mono", monospace',
                  fontSize: 12,
                  overflow: "scroll",
                  height: "100%",
                  flex: "2"
                }}
              />
            </Form.Field>
            <Button
              data-orko="test"
              disabled
              title="Tests the script with user-requested inputs, mocking any risky operations such as trading."
              style={{ alignSelf: "flex-start" }}
            >
              Testing coming soon
            </Button>
          </Grid.Column>
          <Grid.Column width={6} style={{ height: "100%" }}>
            <Form.Field>
              <label>Parameters</label>
              {!!state.parameters && state.parameters.length > 0 && (
                <Table
                  celled
                  striped
                  selectable={!!onEditParameter}
                  style={{ marginTop: 0 }}
                >
                  <Table.Header>
                    <Table.Row>
                      <Table.HeaderCell>Name</Table.HeaderCell>
                      <Table.HeaderCell>Description</Table.HeaderCell>
                      <Table.HeaderCell>Default</Table.HeaderCell>
                      <Table.HeaderCell>Reqd</Table.HeaderCell>
                    </Table.Row>
                  </Table.Header>
                  <Table.Body>
                    {state.parameters.map(parameter => (
                      <Table.Row
                        key={parameter.name}
                        onClick={() => onEditParameter(parameter)}
                      >
                        <Table.Cell>{parameter.name}</Table.Cell>
                        <Table.Cell>{parameter.description}</Table.Cell>
                        <Table.Cell>{parameter.default}</Table.Cell>
                        <Table.Cell>
                          {parameter.mandatory ? (
                            <Icon name="checkmark" />
                          ) : null}
                        </Table.Cell>
                      </Table.Row>
                    ))}
                  </Table.Body>
                </Table>
              )}
              {onAddParameter && (
                <Button data-orko="addParameter" onClick={onAddParameter}>
                  Add
                </Button>
              )}
            </Form.Field>
          </Grid.Column>
        </Grid.Row>
      </Grid>
    </Form>
  )

export default ScriptEditor
