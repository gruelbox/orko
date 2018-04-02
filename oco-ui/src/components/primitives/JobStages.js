import styled from 'styled-components';
import { fontSize, color, fontWeights } from 'styled-system';

const JobStages = styled.ul.attrs({
  color: "fore",
})`
  ${color}
  ${fontSize}
  ${fontWeights}
  list-style-type: none;
  padding: 0 0 0 ${props => props.theme.space[3] + "px"};
`;

export default JobStages;