import styled from 'styled-components';
import { fontSize, color, fontWeight } from 'styled-system';

const Para = styled.p.attrs({
  fontSize: 1,
  color: "fore"
})`
  ${color}
  ${fontSize}
  ${fontWeight}
`;

export default Para;