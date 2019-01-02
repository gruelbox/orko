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
import styled from "styled-components"
import { space, fontSize, color, fontWeight } from "styled-system"
import { rgba } from "polished"

const Tab = styled.button`
  border-radius: 2px;
  color: ${props => (props.selected ? "black" : props.theme.colors.fore)};
  background: ${props =>
    props.selected ? rgba(255, 255, 255, 0.9) : rgba(255, 255, 255, 0.05)};
  border-color: transparent;
  border-width: 2px;
  border-style: solid;
  padding: 0 ${props => props.theme.space[1] + "px"} 0 ${props =>
  props.theme.space[1] + "px"};
  font-size: ${props => props.theme.fontSizes[1] + "px"}
  margin: 0 0 0 ${props => props.theme.space[2] + "px"};
  &:hover {
    color: ${props => (props.selected ? "black" : props.theme.colors.emphasis)};
    cursor: ${props => (props.selected ? "auto" : "pointer")};
    background-color: ${props =>
      props.selected ? rgba(255, 255, 255, 0.7) : rgba(255, 255, 255, 0.1)};
  }
  display: ${props => (props.visible === false ? "none" : "inline")};
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

export default Tab
