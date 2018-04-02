import React from 'react';
import styled from 'styled-components';
import { fontSize, color, fontWeights, space } from 'styled-system';
import { darken, mix } from 'polished'

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
  border: 1px solid ${props => props.error
    ? mix(0.8, props.theme.colors.inputBorder, '#FF0000')
    : props.theme.colors.inputBorder};
  background-color: ${props => props.error
    ? mix(0.9, props.theme.colors.inputBg, '#FF0000')
    : props.theme.colors.inputBg};
  font-family: ${props => props.theme.fonts[0]};
  &:hover {
    background-color: ${props => darken(0.05, props.theme.colors.inputBg)};
  }
  &:focus {
    background-color: ${props => darken(0.1, props.theme.colors.inputBg)};
  }
  ${color}
  ${fontSize}
  ${fontWeights}
  ${space}
`;

const RawLabel = styled.label.attrs({
  fontSize: 1,
  color: "fore",
  mt: 0,
  mb: 1,
  p: 0
})`
  ${color}
  ${fontSize}
  ${fontWeights}
  ${space}
`;

const RawFieldSet = styled.fieldset`
  border: none;
  margin-top: ${props => props.theme.space[2] + "px"};
  margin-bottom: ${props => props.theme.space[2] + "px"};
  margin-left: 0;
  margin-right: 0;
  padding: 0;
`;

const Input = props => (
  <RawFieldSet>
    {props.label &&
      <RawLabel for={props.id}>{props.label}</RawLabel>
    }
    <RawInput
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