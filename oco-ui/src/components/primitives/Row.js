import styled from 'styled-components';
import { fontSize, color, fontWeight, space } from 'styled-system';
import { darken } from 'polished';

const Row = styled.tr`
    ${color}
    ${fontSize}
    ${fontWeight}
    ${space}
    &:hover {
        background-color: ${props => darken(0.05, props.theme.colors.inputBg)};
    }
`

export default Row;