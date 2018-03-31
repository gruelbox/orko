import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';
import { Link } from 'react-router-dom';

const ForeLink = styled(Link).attrs({
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

export default ForeLink;