import styled from "styled-components"
import { fontSize, color, fontWeight, fontFamily, space } from "styled-system"

const Heading = styled.h3.attrs({
  fontSize: 2,
  fontWeight: 700,
  fontFamily: "heading"
})`
  ${color}
  ${fontSize}
  ${fontFamily}
  ${fontWeight}
  ${space}
  text-transform: uppercase;
  display: inline;
  white-space: nowrap;
`

export default Heading
