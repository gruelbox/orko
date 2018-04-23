import styled from 'styled-components';
import { spacing } from 'styled-system';

const HeaderCell = styled.th`
    font-size: ${props => props.theme.fontSizes[1] + "px"};
    color: ${props => props.theme.colors.fore};
    border-bottom: 1px solid ${props => props.theme.colors.deemphasis};
    margin-top: "0px";
    margin-left: ${props => props.theme.space[1] + "px"};
    margin-bottom: ${props => props.theme.space[3] + "px"};
    margin-right: ${props => props.theme.space[1] + "px"};
    text-align: ${props => props.number ? "right" : "left"};
    ${spacing}
`;

export default HeaderCell;