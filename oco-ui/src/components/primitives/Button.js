import styled from "styled-components"
import { fontSize, color, space, fontFamily } from "styled-system"
import { darken, lighten } from "polished"

const buttonColor = props =>
  props.bg ? props.theme.colors[props.bg] : props.theme.colors.link

const Button = styled.button.attrs({
  fontSize: 2,
  color: "white",
  mt: 2,
  p: 2,
  type: "button",
  fontFamily: "heading"
})`
  text-transform: uppercase;
  font-weight: bold;
  border: 1px solid ${buttonColor};
  border-radius: ${props => props.theme.radii[2] + "px"};
  background-color: ${buttonColor};
  &:hover {
    cursor: pointer;
    background-color: ${props => darken(0.1, buttonColor(props))};
    border: 1px solid ${props => darken(0.1, buttonColor(props))};
  }
  &:active {
    background-color: ${props => lighten(0.1, buttonColor(props))};
    border: 1px solid ${props => lighten(0.1, buttonColor(props))};
  };
  &:disabled {
    cursor: auto;
    color: ${props => props.theme.colors.disabled};
    background-color: ${props => props.theme.colors.disabledBg};
    border: 1px solid ${props => props.theme.colors.disabledBg};
  };
  width: ${props => (props.width ? props.width + "px" : "auto")};
  ${color}
  ${fontSize}
  ${fontFamily}
  ${space}
`

export default Button
