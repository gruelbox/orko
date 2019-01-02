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
import { fontSize, color, fontWeight, space } from "styled-system"
import FormButtonBar from "./FormButtonBar"
import RawForm from "./RawForm"

const RawFormContent = styled.div`
  flex: 1;
  display: flex;
  flex-direction: ${props =>
    props.flexDirection ? props.flexDirection : "row"};
  justify-content: flex-start;
  align-items: flex-start;
  align-content: flex-start;
  flex-wrap: wrap;
  width: 100%;
  height: 100%;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const Form = props => (
  <RawForm data-orko={props["data-orko"]}>
    <RawFormContent flexDirection={props["flex-direction"]}>
      {props.children}
    </RawFormContent>
    {props.buttons && <FormButtonBar>{props.buttons()}</FormButtonBar>}
  </RawForm>
)

export default Form
