import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';

const ForeSpan = styled.span.attrs({
  fontSize: 1,
  color: "fore"
})`
  ${color}
  ${fontSize}
  ${fontWeights}
`;

export default ForeSpan;