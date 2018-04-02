import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';

const Href = styled.a.attrs({
  fontSize: 1,
  color: "fore"
})`
  &:hover {
    color: ${props => props.theme.colors.link};
  }
  cursor: pointer;
  ${color}
  ${fontSize}
  ${fontWeights}
`;

export default Href;