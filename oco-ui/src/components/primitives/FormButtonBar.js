import styled from "styled-components"

const FormButtonBar = styled.div`
  margin-top: ${props => props.theme.space[2] + "px"};
  align-self: flex-end;
  border-top: 1px solid rgba(0, 0, 0, 0.2);
  display: flex;
  justify-content: flex-end;
  width: 100%;
  & > button {
    margin-left: ${props => props.theme.space[2] + "px"};
  }
  & > div {
    margin-left: ${props => props.theme.space[2] + "px"};
  }
  padding-left: ${props => props.theme.space[2] + "px"};
  padding-right: ${props => props.theme.space[2] + "px"};
  padding-top: ${props => props.theme.space[1] + "px"};
  padding-bottom: ${props => props.theme.space[1] + "px"};
`

export default FormButtonBar
