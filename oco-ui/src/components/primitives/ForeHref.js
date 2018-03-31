import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';

const ForeHref = styled.a.attrs({
  fontSize: 1,
  color: "fore"
})`
  ${color}
  ${fontSize}
  ${fontWeights}
`;

export default ForeHref;