import styled from 'styled-components';
import { fontSize, color, fontWeight, space } from 'styled-system';

const Span = styled.span.attrs({
  fontSize: 1
})`
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`;

export default Span;