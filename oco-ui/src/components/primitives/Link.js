import styled from 'styled-components';
import { fontSize, color, fontWeight } from 'styled-system';
import { Link as ReactLink } from 'react-router-dom';

const Link = styled(ReactLink)`
  &:hover {
    color: ${props => props.theme.colors.link};
  }
  cursor: pointer;
  color: ${props => props.theme.colors.fore};
  font-size: ${props => props.theme.fontSizes[1] + "px"};
  ${color}
  ${fontSize}
  ${fontWeight}
`;

export default Link;