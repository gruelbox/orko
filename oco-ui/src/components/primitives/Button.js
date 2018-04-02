import styled from 'styled-components';
import { fontSize, color, fontFamily, space } from 'styled-system';
import { darken, lighten } from 'polished';

const Button = styled.button.attrs({
  fontSize: 2,
  color: "white",
  bg: 'link',
  my: 2,
  px: 3,
  py: 2,
  type: "button"
})`
  font-family: ${props => props.theme.fonts[0]};
  text-transform: uppercase;
  font-weight: bold;
  border: 1px solid ${props => props.theme.colors.link};
  border-radius: ${props => props.theme.radii[1] + "px"};
  &:hover {
    cursor: pointer;
    background-color: ${props => darken(0.1, props.theme.colors.link)};
    border: 1px solid ${props => darken(0.1, props.theme.colors.link)};
  }
  &:active {
    background-color: ${props => lighten(0.1, props.theme.colors.link)};
    border: 1px solid ${props => lighten(0.1, props.theme.colors.link)};
  };
  &:disabled {
    cursor: auto;
    color: ${props => props.theme.colors.fore};
    background-color: ${props => props.theme.colors.inputBg};
    border: 1px solid ${props =>  props.theme.colors.inputBg};
  };
  ${color}
  ${fontSize}
  ${space}
  ${fontFamily}
`;

export default Button;