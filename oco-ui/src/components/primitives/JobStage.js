import styled from 'styled-components';
import { fontSize, color, fontWeight } from 'styled-system';

const JobStage = styled.li.attrs({
  fontSize: 1,
  color: "fore"
})`
  ${color}
  ${fontSize}
  ${fontWeight}
`;

export default JobStage;