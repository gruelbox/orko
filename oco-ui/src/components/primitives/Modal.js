import React from "react"
import styled from "styled-components"

const Content = styled.div`
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  margin: auto;
  width: ${props => props.mobile ? "100%": "500px"};
  height: ${props => props.mobile ? "100%": "500px"};
  background-color: ${props => props.theme.colors.backgrounds[3]};
  box-shadow: 0 0 16px rgba(0, 0, 0, .7);
`

const Modal = ({ children, mobile }) => (
  <Content mobile={mobile}>{children}</Content>
)

export default Modal