import { lighten } from 'polished';
import { Tab as RebassTab } from 'rebass';

const Tab = RebassTab.extend`
  color: ${props => props.selected ? props.theme.colors.emphasis : props.theme.colors.fore};
  border-color: ${props => props.selected ? props.theme.colors.emphasis : "none"};
  &:hover {
    cursor: ${props => props.selected ? "auto" : "pointer"};
    color: ${props => props.selected ? props.theme.colors.emphasis : lighten(0.1, props.theme.colors.fore)};
    border-color: ${props => props.selected ? props.theme.colors.emphasis : lighten(0.1, props.theme.colors.fore)};
  }
`;

export default Tab;