import styled from 'styled-components';
import { fontSize, color, fontWeights, space } from 'styled-system';
import { darken, lighten } from 'polished'

const Input = styled.input.attrs({
  fontSize: 2,
  color: "fore",
  my: 2,
  mx: 0,
  px: 2,
  py: 2,
})`
  box-shadow: none;
  outline: none;
  display: block;
  border: 1px solid ${props => props.willReceiveValues
    ? props.theme.colors.link
    : props.theme.colors.inputBorder};
  background-color: ${props => props.theme.colors.inputBg};
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

export default Input;