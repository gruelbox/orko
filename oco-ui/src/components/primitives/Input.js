import React from 'react';
import styled from 'styled-components';
import { fontSize, padding, color, fontWeight, space, fontFamily } from 'styled-system';
import { lighten } from 'polished'

const RawInput = styled.input.attrs({
  fontSize: 2,
  color: "fore",
  mb: 0,
  mt: 0,
  mx: 0,
  px: 2,
  py: 2,
})`
  box-shadow: none;
  outline: none;
  display: block;
  border: 1px solid ${props => props.theme.colors.inputBorder};
  border-radius: ${props => props.theme.radii[1] + "px"};
  box-shadow: ${props => props.error
    ? "0 0 4px red"
    : "none"};
  background-color: ${props => props.theme.colors.inputBg};
  font-family: ${props => props.theme.fonts[0]};
  &:focus {
    border: 1px solid ${props => lighten(0.2, props.theme.colors.inputBorder)};
  }
  width: 120px;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${fontFamily}
  ${space}
`;

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
  ${padding}
`;

const RawFieldSet = styled.fieldset`
  display: inline;
  border: none;
  margin-top: ${props => props.theme.space[2] + "px"};
  margin-bottom: ${props => props.theme.space[2] + "px"};
  margin-left: 0;
  margin-right: ${props => props.theme.space[2] + "px"};
  padding: 0;
`;

const Input = props => (
  <RawFieldSet>
    {props.label &&
      <RawLabel for={props.id}>{props.label}</RawLabel>
    }
    <RawInput
      id={props.id}
      error={props.error}
      type={props.type}
      placeholder={props.placeholder}
      value={props.value}
      onChange={props.onChange}
      onFocus={props.onFocus}
    />
  </RawFieldSet>
);

export default Input;