import styled from "styled-components"

const FormButtonBar = styled.div`
  margin-top: ${props => props.theme.space[2] + "px"};
  align-self: flex-end;
  border-top: 1px solid rgba(0, 0, 0, 0.2);
  display: flex;
  justify-content: flex-end;
  width: 100%;
  & > button {
    margin-top: 0
    margin-left: ${props => props.theme.space[2] + "px"};
    margin-bottom: 0
  }
  & > div {
    margin-top: 0 !important;
    margin-left: ${props => props.theme.space[2] + "px"} !important;
    margin-bottom: 0 !important;
  }
  & > label {
    margin-left: ${props => props.theme.space[2] + "px"};
  }
  padding-left: ${props => props.theme.space[2] + "px"};
  padding-right: ${props => props.theme.space[2] + "px"};
  padding-top: ${props => props.theme.space[2] + "px"};
  padding-bottom: ${props => props.theme.space[1] + "px"};
`

export default FormButtonBar
