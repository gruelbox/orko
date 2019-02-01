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
import { fontSize, color, space, fontFamily } from "styled-system"
import { darken, lighten } from "polished"

const buttonColor = props =>
  props.bg ? props.theme.colors[props.bg] : props.theme.colors.link

const Button = styled.button.attrs({
  fontSize: 2,
  color: "white",
  mt: 2,
  p: 2,
  type: "button",
  fontFamily: "heading"
})`
  text-transform: uppercase;
  font-weight: bold;
  border: 1px solid ${buttonColor};
  border-radius: ${props => props.theme.radii[2] + "px"};
  background-color: ${buttonColor};
  &:hover {
    cursor: pointer;
    background-color: ${props => darken(0.1, buttonColor(props))};
    border: 1px solid ${props => darken(0.1, buttonColor(props))};
  }
  &:active {
    background-color: ${props => lighten(0.1, buttonColor(props))};
    border: 1px solid ${props => lighten(0.1, buttonColor(props))};
  };
  &:disabled {
    cursor: auto;
    color: ${props => props.theme.colors.disabled};
    background-color: ${props => props.theme.colors.disabledBg};
    border: 1px solid ${props => props.theme.colors.disabledBg};
  };
  width: ${props => (props.width ? props.width + "px" : "auto")};
  ${color}
  ${fontSize}
  ${fontFamily}
  ${space}
`

export default Button
