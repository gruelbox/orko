import React from 'react';
import styled from 'styled-components';
import { fontSize, color, fontWeight, fontFamily, space } from 'styled-system';
import Href from './Href';
import { Icon } from 'semantic-ui-react';

const SectionHeadingText = styled.h3.attrs({
  fontSize: 2,
  fontWeight: 700,
  color: "heading",
  fontFamily: 'sans',
  p: 0,
  m: 0
})`
  ${color}
  ${fontSize}
  ${fontFamily}
  ${fontWeight}
  ${space}
  text-transform: uppercase;
  display: inline;
`;

const SectionHeadingBox = styled.div.attrs({
  pb: 1,
  pt: 1
})`
  ${space}
  margin-bottom: ${props => props.expanded ? props.theme.space[2] + "px" : "0"};
  border-bottom: ${props => props.expanded ? "1px solid" + props.theme.colors.deemphasis : "none"};
  &:last-child {
    margin-bottom: ${props => props.theme.space[2] + "px"};
  }
  display: flex;
  justify-content: space-between;
`;

const Toggle = Href.extend.attrs({
  color: "heading"
})`
  ${color}
  cursor: pointer;
`;

const SectionBox = styled.div.attrs({
  pb: 0,
  pt: 1,
  mb: 0
})`
  ${space}
`;

class Section extends React.Component {

  constructor(props) {
    super(props);
    this.state = { expanded: true };
  }

  toggle = () => {
    console.log("Toggle expansion");
    this.setState(prev => ({
      expanded: !prev.expanded 
    }));
  }

  render() {
    return (
      <SectionBox>
        <SectionHeadingBox expanded={this.state.expanded}>
          <SectionHeadingText>{this.props.heading}</SectionHeadingText>
          <Toggle onClick={this.toggle}><Icon name={ this.state.expanded ? "angle up" : "angle down"}/></Toggle>
        </SectionHeadingBox>
        {this.state.expanded &&
          <div>
            {this.props.children}
          </div>
        }
      </SectionBox>
    );
  }
}

export default Section;