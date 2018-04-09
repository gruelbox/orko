import styled from 'styled-components';
import { fontSize, color, fontWeight, space } from 'styled-system';

const Table = styled.table`
  color: ${props => props.theme.colors.fore};
  border-collapse: collapse;
  width: 100%;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`;

export default Table;