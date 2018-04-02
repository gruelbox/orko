import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';
import { Link as ReactLink } from 'react-router-dom';

const Link = styled(ReactLink).attrs({
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

export default Link;