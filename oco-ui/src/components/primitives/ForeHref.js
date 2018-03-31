import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';

const ForeHref = styled.a.attrs({
  fontSize: 1,
  color: "fore"
})`
  &:hover {
    color: ${props => props.theme.colors.link};
  }
  ${color}
  ${fontSize}
  ${fontWeights}
`;

export default ForeHref;