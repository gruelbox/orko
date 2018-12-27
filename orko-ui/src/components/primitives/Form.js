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
