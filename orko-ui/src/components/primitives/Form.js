import React from "react"
import styled from "styled-components"
import { fontSize, color, fontWeight, space } from "styled-system"
import FormButtonBar from "./FormButtonBar"

const RawForm = styled.form`
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  align-content: stretch;
  width: 100%;
  height: 100%;
`

const RawFormContent = styled.div`
  flex: 1;
  display: flex;
  flex-direction: row;
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
    <RawFormContent>{props.children}</RawFormContent>
    {props.buttons && <FormButtonBar>{props.buttons()}</FormButtonBar>}
  </RawForm>
)

export default Form
