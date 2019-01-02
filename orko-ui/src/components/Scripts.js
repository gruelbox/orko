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
