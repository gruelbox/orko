import styled from 'styled-components';
import { space, padding } from 'styled-system'

const DarkComponentBox = styled.div.attrs({
  pb: 3
})`
  background-color: ${props => props.theme.colors.backgrounds[1]};
  border: 1px solid ${props => props.theme.colors.boxBorder};
  ${space}
  ${padding}
`;

export default DarkComponentBox;