import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';

const SectionHeading = styled.h3.attrs({
  fontSize: 2,
  fontWeight: "bold",
  color: "heading"
})`
  ${color}
  ${fontSize}
  ${fontWeights}
  text-transform: uppercase
`;

export default SectionHeading;