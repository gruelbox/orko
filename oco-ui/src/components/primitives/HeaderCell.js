import styled from 'styled-components';

const HeaderCell = styled.th`
    font-size: ${props => props.theme.fontSizes[0] + "px"};
    color: ${props => props.theme.colors.heading};
    border-bottom: 1px solid ${props => props.theme.colors.deemphasis};
    margin-top: "0px";
    margin-left: ${props => props.theme.space[1] + "px"};
    margin-bottom: ${props => props.theme.space[3] + "px"};
    margin-right: ${props => props.theme.space[1] + "px"};
    text-align: ${props => props.number ? "right" : "left"};
`;

export default HeaderCell;