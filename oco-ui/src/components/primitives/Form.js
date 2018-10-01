import styled from "styled-components"
import { fontSize, color, fontWeights, spacing } from "styled-system"

const Form = styled.form.attrs({})`
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  width: 100%;
  height: 100%;
  ${color}
  ${fontSize}
  ${fontWeights}
  ${spacing}
`

export default Form
