import React from "react"
import styled from "styled-components"
import { color } from "styled-system"
import { Icon } from "semantic-ui-react"
import Heading from "./Heading"

const SectionBox = styled.section`
  ${color}
  margin: 0;
  padding: 0;
  height: 100%;
`

const SectionHeadingBox = styled.div`
  position: sticky;
  left: 0;
  top: 0;
  padding: ${props => props.theme.space[2] + "px"};
  border-bottom: 1px solid ${props => props.theme.colors.deemphasis}
  display: flex;
  justify-content: space-between;
`

const SectionInner = styled.section`
  padding-top: 12px;
  padding-bottom: ${props => props.theme.space[2] + "px"};
  padding-left: ${props => props.theme.space[2] + "px"};
  padding-right: ${props => props.theme.space[2] + "px"};
  height: ${props => props.expand ? "100%" : "auto"}
`

class Section extends React.Component {
  render() {
    return (
      <SectionBox>
        <SectionHeadingBox>
          <Heading p={0} m={0} color="heading">
            <Icon className="dragMe" name="content"/>
            {this.props.heading}
          </Heading>
        </SectionHeadingBox>
        <SectionInner expand={this.props.expand}>
          {this.props.children}
        </SectionInner>
      </SectionBox>
    )
  }
}

export default Section
