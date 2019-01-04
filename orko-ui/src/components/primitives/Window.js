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
import { Rnd } from "react-rnd"
import theme from "../../theme"

const Content = styled.div`
  background-color: ${props => props.theme.colors.backgrounds[2]};
`

const FloatingContent = styled(Content)`
  z-index: 10;
  text-align: left;
`

const Window = ({ children, mobile, large }) =>
  mobile ? (
    <Content>{children}</Content>
  ) : (
    <Rnd
      default={{ x: 100, y: 100 }}
      style={{
        border: "1px solid " + theme.colors.canvas,
        boxShadow: "0 0 16px rgba(0, 0, 0, 0.4)",
        zIndex: 999
      }}
      dragHandleClassName="dragMe"
      enableResizing={false}
    >
      <FloatingContent>{children}</FloatingContent>
    </Rnd>
  )

export default Window
