import styled from 'styled-components';
import { space } from 'styled-system'

const MidComponentBox = styled.div`
  background-color: ${props => props.theme.ocoComponentBg1}
  ${space}
`;

export default MidComponentBox;