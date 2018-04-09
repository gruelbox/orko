import styled from 'styled-components';
import { color } from 'styled-system';

const Cell = styled.td`
  font-size: ${props => props.theme.fontSizes[1] + "px"};
  margin: ${props => props.theme.space[1] + "px"};
  text-align: ${props => props.number ? "right" : "left"};
  ${color}
`;

export default Cell;