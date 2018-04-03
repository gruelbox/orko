import styled from 'styled-components';
import { fontSize, color, fontWeight, space } from 'styled-system';

const Href = styled.a`
  &:hover {
    color: ${props => props.theme.colors.link};
  }
  cursor: pointer;
  color: ${props => props.theme.colors.fore};
  font-size: ${props => props.theme.fontSizes[1] + "px"};
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`;

export default Href;