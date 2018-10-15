import styled from 'styled-components';
import { fontSize, color, fontWeight, space } from 'styled-system';
import { Link as ReactLink } from 'react-router-dom';

const Link = styled(ReactLink)`
  cursor: pointer;
  font-size: ${props => props.theme.fontSizes[1] + "px"};
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`;

export default Link;