import React from "react"
import styled from "styled-components"
import { color, space } from "styled-system"
import Href from "./Href"
import { Icon } from "semantic-ui-react"
import Heading from "./Heading"

const SectionHeadingBox = styled.div.attrs({
  pb: 1,
  pt: 1
})`
  ${space}
  margin-bottom: ${props =>
    props.expanded ? props.theme.space[2] + "px" : "0"};
  border-bottom: ${props =>
    props.expanded ? "1px solid" + props.theme.colors.deemphasis : "none"};
  &:last-child {
    margin-bottom: ${props => props.theme.space[2] + "px"};
  }
  display: flex;
  justify-content: space-between;
`

const Toggle = Href.extend.attrs({
  color: "heading"
})`
  ${color}
  cursor: pointer;
`

const SectionBox = styled.div`
  ${space}
  ${color}
  padding: ${props => props.theme.space[2] + "px"};
  margin: 0;
  height: ${props => props.expand ? "100%" : "auto"}
`

class Section extends React.Component {
  constructor(props) {
    super(props)
    this.storageKey = "section.expanded." + props.id
    const savedState = localStorage.getItem(this.storageKey)
    this.state = { expanded: savedState !== "false" }
  }

  toggle = () => {
    console.log("Toggle expansion")
    this.setState(
      prev => ({
        expanded: !prev.expanded
      }),
      () => localStorage.setItem(this.storageKey, this.state.expanded)
    )
  }

  render() {
    return (
      <SectionBox expand={this.props.expand}>
        <SectionHeadingBox expanded={this.state.expanded}>
          <Heading p={0} m={0} color="heading">
            {this.props.heading}
          </Heading>
          <Toggle onClick={this.toggle}>
            <Icon name={this.state.expanded ? "angle up" : "angle down"} />
          </Toggle>
        </SectionHeadingBox>
        {this.state.expanded && <div style={{height: "100%"}}>{this.props.children}</div>}
      </SectionBox>
    )
  }
}

export default Section
