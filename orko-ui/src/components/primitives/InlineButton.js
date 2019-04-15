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
import { fontSize, space, fontFamily } from "styled-system"
import { mix, lighten } from "polished"

const buttonColor = props =>
  mix(0.4, props.theme.colors.inputBg, props.theme.colors[props.color])

const Container = styled.div`
  position: absolute;
  right: 4.1em;
  top: 0.7em;
  display: flex;
  flex-direction: row-reverse;
`

const Button = styled.button`
  font-weight: bold;
  text-transform: uppercase;
  font-size: 0.8em;
  border: none;
  background-color: ${buttonColor};
  &:hover {
    cursor: pointer;
    background-color: ${props => lighten(0.1, buttonColor(props))};
  }
  &:active {
    background-color: ${props => lighten(0.2, buttonColor(props))};
  };
  height: 1.3em;
  margin-left: 2px;
  color: black;
  ${fontSize}
  ${fontFamily}
  ${space}
`

export default {
  Button,
  Container
}
