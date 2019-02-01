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

const FormButtonBar = styled.div`
  margin-top: ${props => props.theme.space[2] + "px"};
  align-self: flex-end;
  border-top: 1px solid rgba(0, 0, 0, 0.2);
  display: flex;
  justify-content: flex-end;
  width: 100%;
  & > button {
    margin-top: 0
    margin-left: ${props => props.theme.space[2] + "px"};
    margin-bottom: 0
  }
  & > div {
    margin-top: 0 !important;
    margin-left: ${props => props.theme.space[2] + "px"} !important;
    margin-bottom: 0 !important;
  }
  & > label {
    margin-left: ${props => props.theme.space[2] + "px"};
  }
  padding-left: ${props => props.theme.space[2] + "px"};
  padding-right: ${props => props.theme.space[2] + "px"};
  padding-top: ${props => props.theme.space[2] + "px"};
  padding-bottom: ${props => props.theme.space[1] + "px"};
`

export default FormButtonBar
