import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';
import { Link } from 'react-router-dom';

const ForeLink = styled(Link).attrs({
  fontSize: 1,
  color: "fore"
})`
  ${color}
  ${fontSize}
  ${fontWeights}
`;

export default ForeLink;