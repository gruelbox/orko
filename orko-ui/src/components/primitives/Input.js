import React from "react"
import styled from "styled-components"
import { fontSize, color, fontWeight, space } from "styled-system"

const RawInput = styled.input.attrs({
  fontSize: 2,
  color: "fore",
  m: 0,
  px: 2,
  py: 2
})`
  box-shadow: none;
  outline: none;
  display: block;
  border: 2px solid ${props =>
    props.error
      ? props.theme.colors.inputBorderError
      : props.theme.colors.inputBg};
  border-radius: ${props => props.theme.radii[2] + "px"};
  background-color: ${props => props.theme.colors.inputBg};
  color: ${props => (props.error ? props.theme.colors.alert : "inherit")};
  &:focus {
    border: 2px solid ${props =>
      props.error
        ? props.theme.colors.inputBorderError
        : props.theme.colors.inputBorder};
  }
  width: 100%;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const RawLabel = styled.label.attrs({
  fontSize: 2,
  color: "fore",
  mt: 0,
  mb: 1,
  p: 0
})`
  display: block;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const RawFieldSet = styled.fieldset`
  display: inline;
  border: none;
  width: 140px;
  flex: ${props => props.flex};
  ${space}
`

const Input = props => (
  <RawFieldSet mr={props.mr ? props.mr : 2} flex={props.flex} ml={0} p={0}>
    {props.label && (
      <RawLabel htmlFor={props.id} disabled={props.disabled}>
        {props.label}
      </RawLabel>
    )}
    <RawInput
      id={props.id}
      error={props.error}
      type={props.type}
      placeholder={props.placeholder}
      value={props.value}
      onChange={props.onChange}
      onFocus={props.onFocus}
      disabled={props.disabled}
    />
  </RawFieldSet>
)

export default Input
