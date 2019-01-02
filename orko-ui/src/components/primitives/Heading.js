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
import { fontSize, color, fontWeight, fontFamily, space } from "styled-system"

const Heading = styled.h3.attrs({
  fontSize: 2,
  fontWeight: 700,
  fontFamily: "heading"
})`
  ${color}
  ${fontSize}
  ${fontFamily}
  ${fontWeight}
  ${space}
  text-transform: uppercase;
  display: inline;
  white-space: nowrap;
`

export default Heading
