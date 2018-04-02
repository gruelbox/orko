import styled from 'styled-components';
import { fontSize, color, fontWeights, space } from 'styled-system';

const Span = styled.span.attrs({
  fontSize: 1
})`
  ${color}
  ${fontSize}
  ${fontWeights}
  ${space}
`;

export default Span;