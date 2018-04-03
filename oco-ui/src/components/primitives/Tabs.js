import { Tabs as RebassTabs } from 'rebass';

const Tabs = RebassTabs.extend`
  border-color: ${props => props.theme.colors.deemphasis};
`;

export default Tabs;