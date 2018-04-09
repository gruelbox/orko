import styled from 'styled-components';

const Cell = styled.td`
  font-size: ${props => props.theme.fontSizes[0] + "px"};
  margin: ${props => props.theme.space[1] + "px"};
  text-align: ${props => props.number ? "right" : "left"};
`;

export default Cell;