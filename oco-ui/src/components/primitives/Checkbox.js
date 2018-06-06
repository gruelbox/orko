import React from "react"
import styled from "styled-components"
import { fontSize, padding, color, fontWeight, space } from 'styled-system';

const RawFieldSet = styled.fieldset.attrs({
  my: 2,
  ml: 0,
  mr: 3,
  p: 0
})`
  display: inline;
  border: none;
  ${space}
  ${padding}
`

const RawLabel = styled.label.attrs({
  fontSize: 2,
  color: "fore",
  mt: 0,
  mb: 1,
  mr: 2,
  p: 0
})`
  display: inline;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
  ${padding}
`

const Checkbox = props => (
  <RawFieldSet>
    {props.label && <RawLabel for={props.id}>{props.label}</RawLabel>}
    <input
      id={props.id}
      type={props.type}
      checked={props.checked}
      onChange={props.onChange}
      onFocus={props.onFocus}
    />
  </RawFieldSet>
)

export default Checkbox
