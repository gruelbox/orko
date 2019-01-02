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
import styled from "styled-components"
import { lighten } from "polished"

const Content = styled.div`
  margin-top: ${props => props.theme.space[2] + "px"};
  margin-right: ${props => props.theme.space[3] + "px"};
  display: inline-block;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center; 
  & label {
    cursor: pointer;
    padding-left: 28px;
    :before {
      content: "";
      position: absolute;
      width: 20px;
      height: 20px;
      left: 0;
      cursor: pointer;
      background-color: ${props => props.theme.colors.inputBg};
      border: 4px solid ${props => props.theme.colors.inputBg};
      border-radius: ${props => props.theme.radii[2] + "px"};
      box-shadow: 0 0 10px ${props => props.theme.colors.backgrounds[4]};
    }
  }
  & input {
    display: none;
    &:checked + label:before {
      background-color: ${props => lighten(0.2, props.theme.colors.emphasis)};
    }
    &:checked {
      box-shadow: 0 0 3px black inset;
    }
  }
}
`

const Checkbox = props => (
  <Content title={props.title}>
    <input
      data-orko={props.id}
      id={props.id}
      type={props.type}
      checked={props.checked}
      onChange={props.onChange}
      onFocus={props.onFocus}
    />
    <label htmlFor={props.id}>{props.label}</label>
  </Content>
)

export default Checkbox
