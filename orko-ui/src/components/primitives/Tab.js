import styled from "styled-components"
import { space, fontSize, color, fontWeight } from "styled-system"
import { rgba } from "polished"

const Tab = styled.button`
  border-radius: 2px;
  color: ${props => (props.selected ? "black" : props.theme.colors.fore)};
  background: ${props =>
    props.selected ? rgba(255, 255, 255, 0.7) : rgba(255, 255, 255, 0.05)};
  border-color: transparent;
  border-width: 2px;
  border-style: solid;
  padding: 0 ${props => props.theme.space[1] + "px"} 0 ${props =>
  props.theme.space[1] + "px"};
  font-size: ${props => props.theme.fontSizes[1] + "px"}
  margin: 0 0 0 ${props => props.theme.space[2] + "px"};
  &:hover {
    color: ${props => (props.selected ? "black" : props.theme.colors.emphasis)};
    cursor: ${props => (props.selected ? "auto" : "pointer")};
    background-color: ${props =>
      props.selected ? rgba(255, 255, 255, 0.7) : rgba(255, 255, 255, 0.1)};
  }
  display: ${props => (props.visible === false ? "none" : "inline")};
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

export default Tab
