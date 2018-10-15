import styled from 'styled-components';
import { fontSize, color, space, fontWeight } from 'styled-system';

const Para = styled.p.attrs({
  fontSize: 2
})`
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`;

export default Para;