import React from "react"
import styled from "styled-components"
import { fontSize, color, fontWeight, space, width } from "styled-system"

const RawInput = styled.input.attrs({
  fontSize: 2,
  mx: 0,
  mt: 0,
  mb: 2,
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
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
  ${width}
`

const RawLabel = styled.label.attrs({
  fontSize: 2,
  mt: 0,
  mb: 1,
  p: 0
})`
  display: block;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
  ${width}
`

const RawFieldSet = styled.fieldset`
  display: inline;
  border: none;
  flex: ${props => props.flex};
  ${space}
  ${width}
`

const Input = props => (
  <RawFieldSet
    mr={props.mr ? props.mr : 2}
    flex={props.flex}
    ml={0}
    p={0}
    width={props.width}
  >
    {props.label && (
      <RawLabel htmlFor={props.id} disabled={props.disabled}>
        {props.label}
      </RawLabel>
    )}
    <RawInput
      data-orko={props.id}
      id={props.id}
      error={props.error}
      type={props.type}
      placeholder={props.placeholder}
      value={props.value}
      onChange={props.onChange}
      onFocus={props.onFocus}
      disabled={props.disabled}
      width={props.width}
    />
  </RawFieldSet>
)

export default Input
