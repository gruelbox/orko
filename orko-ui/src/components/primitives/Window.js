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
