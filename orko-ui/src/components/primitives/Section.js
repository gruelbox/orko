/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React from "react"
import styled from "styled-components"
import { color } from "styled-system"
import { Icon } from "semantic-ui-react"
import Heading from "./Heading"
import Href from "./Href"
import Span from "./Span"

const SectionBox = styled.section`
  ${color} margin: 0;
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.2);
`

const SectionHeadingBox = styled.div`
  background-color: ${props => props.theme.colors.backgrounds[3]};
  vertical-align: middle;
  padding: ${props => props.theme.space[2] + "px"};
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 0, 0, 0.4);
  box-shadow: 0 2px 16px 0 rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 10;
  overflow: hidden;
`

const SectionInner = styled.section`
  background-color: ${props => props.theme.colors.backgrounds[1]};
  padding-top: ${props => (props.nopadding ? 0 : "10px")};
  padding-bottom: ${props => (props.nopadding ? 0 : props.theme.space[2] + "px")};
  padding-left: ${props => (props.nopadding ? 0 : props.theme.space[2] + "px")};
  padding-right: ${props => (props.nopadding ? 0 : props.theme.space[2] + "px")};
  flex: 1;
  flex-grow: 1;
  position: relative;
  overflow-x: ${props => (props.scroll === "horizontal" || props.scroll === "both" ? "scroll" : "auto")};
  overflow-y: ${props => (props.scroll === "vertical" || props.scroll === "both" ? "scroll" : "auto")};
`

const FloatingIcon = styled.div`
  position: absolute;
  font-size: 36px;
  top: 8px;
  right: 30px;
  z-index: -1;
  opacity: 0.1;
  color: black;
`

class Section extends React.Component {
  render() {
    return (
      <Context.Consumer>
        {context => (
          <SectionBox>
            <SectionHeadingBox
              className={context && context.draggable && !context.compactDragHandle ? "dragMe" : undefined}
              data-orko={"section/" + this.props.id + "/tabs"}
            >
              <Heading p={0} my={0} ml={0} mr={3} color="heading">
                {context && !!context.onHide && (
                  <Href
                    data-orko={"section/" + this.props.id + "/hide"}
                    onClick={context.onHide}
                    title="Hide this panel (it can be shown again from View Settings)"
                    fontSize={2}
                    color="deemphasis"
                  >
                    <Icon name="close" />
                  </Href>
                )}
                {context && context.draggable && context.compactDragHandle && (
                  <Span color="deemphasis">
                    <Icon name="arrows alternate" title="Drag to re-order panel" className="dragMe" />
                  </Span>
                )}
                {context && context.draggable && !!context.onToggleAttached && (
                  <Href
                    data-orko={"section/" + this.props.id + "/toggleAttached"}
                    fontSize={2}
                    color="deemphasis"
                    onClick={context.onToggleAttached}
                  >
                    <Icon name="external square alternate" title="Detach/detach window" />
                  </Href>
                )}
                <Span ml={context && (!!context.onHide || !!context.draggable) ? 1 : 0}>
                  {this.props.heading}
                </Span>
                {context && context.icon && (
                  <FloatingIcon>
                    <Icon name={context.icon} title={this.props.heading} />
                  </FloatingIcon>
                )}
              </Heading>
              <div style={{ whiteSpace: "nowrap" }}>{this.props.buttons && this.props.buttons()}</div>
            </SectionHeadingBox>
            <SectionInner
              data-orko={"section/" + this.props.id}
              scroll={this.props.scroll}
              expand={this.props.expand}
              nopadding={this.props.nopadding}
            >
              {this.props.children}
            </SectionInner>
          </SectionBox>
        )}
      </Context.Consumer>
    )
  }
}

export default Section

const Context = React.createContext()
export const Provider = Context.Provider
