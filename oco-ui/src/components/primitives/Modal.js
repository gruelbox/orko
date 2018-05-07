import React from "react"
import { Dimmer } from "semantic-ui-react"
import styled from "styled-components"

const Content = styled.div`
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  margin: auto;
  width: 500px;
  height: 500px;
  background-color: ${props => props.theme.colors.backgrounds[3]};
  zIndex: -5;
  box-shadow: 0 0 16px rgba(0, 0, 0, .7);
`

const Modal = ({ children }) => (
  <div>
    <Dimmer />
    <Content>{children}</Content>
  </div>
)

export default Modal