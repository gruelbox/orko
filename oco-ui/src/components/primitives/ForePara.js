import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';

const ForePara = styled.p.attrs({
  fontSize: 1,
  color: "fore"
})`
  ${color}
  ${fontSize}
  ${fontWeights}
`;

export default ForePara;