import styled from 'styled-components';
import { space, padding } from 'styled-system';

const Panel = styled.div`
  background-color: ${props => props.theme.colors.panel};
  ${space}
  ${padding}
`;

export default Panel;