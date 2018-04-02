import styled from 'styled-components';
import { fontSize, color, fontWeights, fontFamily, space } from 'styled-system';

const SectionHeading = styled.h3.attrs({
  fontSize: 2,
  fontWeight: "bold",
  color: "heading",
  pb: 1,
  pt: 1,
  mb: 2
})`
  ${color}
  ${fontSize}
  ${fontFamily}
  ${fontWeights}
  ${space}
  border-bottom: 1px solid ${props => props.theme.colors.deemphasis};
  text-transform: uppercase;
  &:last-child {
    margin-bottom: ${props => props.theme.space[2] + "px"};
  }
`;

export default SectionHeading;