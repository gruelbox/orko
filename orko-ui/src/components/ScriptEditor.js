import React from "react"
import { Form, Grid, Table, Icon, Button } from "semantic-ui-react"

const ScriptEditor = ({
  state,
  onChangeState,
  onAddParameter,
  onEditParameter
}) => (
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
            data-orko="verify"
            disabled
            title="Submits the script to the server to check for validity"
            style={{ alignSelf: "flex-start" }}
          >
            Verify not supported yet
          </Button>
        </Grid.Column>
        <Grid.Column width={6} style={{ height: "100%" }}>
          <Form.Field>
            <label>Parameters</label>
            <Table celled striped selectable>
              <Table.Header>
                <Table.Row>
                  <Table.HeaderCell>Name</Table.HeaderCell>
                  <Table.HeaderCell>Description</Table.HeaderCell>
                  <Table.HeaderCell>Default</Table.HeaderCell>
                  <Table.HeaderCell>Reqd</Table.HeaderCell>
                </Table.Row>
              </Table.Header>
              <Table.Body>
                {!!state.parameters &&
                  state.parameters.map(parameter => (
                    <Table.Row
                      key={parameter.name}
                      onClick={() => onEditParameter(parameter)}
                    >
                      <Table.Cell>{parameter.name}</Table.Cell>
                      <Table.Cell>{parameter.description}</Table.Cell>
                      <Table.Cell>{parameter.default}</Table.Cell>
                      <Table.Cell>
                        {parameter.mandatory ? <Icon name="checkmark" /> : null}
                      </Table.Cell>
                    </Table.Row>
                  ))}
              </Table.Body>
            </Table>
            <Button data-orko="addParameter" onClick={onAddParameter}>
              Add
            </Button>
          </Form.Field>
        </Grid.Column>
      </Grid.Row>
    </Grid>
  </Form>
)

export default ScriptEditor
