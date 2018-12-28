import React from "react"
import { List } from "semantic-ui-react"

const Script = ({ script, onSelect, selected, modified, fresh }) => (
  <List.Item active={selected} onClick={() => onSelect && onSelect(script)}>
    <List.Icon name="file code" size="large" verticalAlign="middle" />
    <List.Content>
      <List.Header data-orko={script.id + "/select"}>{script.name}</List.Header>
      <List.Description>
        {fresh ? "New" : modified ? "Modified" : "Saved"}
      </List.Description>
    </List.Content>
  </List.Item>
)

const Scripts = ({ scripts, onSelect, modified, selected }) => (
  <List selection divided>
    {selected === undefined && (
      <Script
        script={{ name: "New" }}
        modified={modified}
        selected={true}
        fresh={true}
      />
    )}
    {scripts.map(script => (
      <Script
        key={script.id}
        script={script}
        selected={selected && selected === script.id}
        modified={selected && selected === script.id && modified}
        onSelect={onSelect}
      />
    ))}
  </List>
)

export default Scripts
