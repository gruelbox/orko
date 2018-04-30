import styled from 'styled-components'
import { space, padding, fontSize, color, fontWeight, fontFamily } from 'styled-system'


const Tab = styled.button`
  border-radius: 2px;
  color: ${props => props.selected ? "black" : props.theme.colors.fore};
  background: ${props => props.selected ? props.theme.colors.emphasis : "none"};
  border: ${props => props.selected ? "2px solid " + props.theme.colors.emphasis : "none"};
  padding: 0 ${props => props.theme.space[1] + "px"} 0 ${props => props.theme.space[1] + "px"};
  font-size: ${props => props.theme.fontSizes[1] + "px"}
  margin: 0 0 0 ${props => props.theme.space[2] + "px"};
  &:hover {
    cursor: ${props => props.selected ? "auto" : "pointer"};
  }
  ${color}
  ${padding}
  ${fontSize}
  ${fontFamily}
  ${fontWeight}
  ${space}
`;

export default Tab;