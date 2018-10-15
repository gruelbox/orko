import React from "react"
import styled from "styled-components"

const Content = styled.div`
  background-color: ${props => props.theme.colors.backgrounds[2]};
`

const FloatingContent = styled(Content)`
  box-shadow: 0 0 16px rgba(0, 0, 0, 0.7);
  z-index: 10;
`

const Modal = ({ children, mobile }) =>
  mobile ? (
    <Content>{children}</Content>
  ) : (
    <FloatingContent>{children}</FloatingContent>
  )

export default Modal
