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
import { Icon } from "semantic-ui-react"
import styled from "styled-components"
import { fontSize, color, fontWeight, space } from "styled-system"
import { formatNumber } from "modules/common/util/numberUtils"
import Loading from "./Loading"
import { connect } from "react-redux"
import { withFramework } from "FrameworkContainer"
import { withServer } from "modules/server"

const PriceKey = styled.div.attrs({
  py: 0,
  px: 1
})`
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const PriceValue = styled.div`
  padding: 3px;
  cursor: copy;
  &:hover {
    color: ${props => props.theme.colors.link};
  };
  background-color: ${props =>
    props.movement === "up"
      ? props.theme.colors.buy
      : props.movement === "down"
      ? props.theme.colors.sell
      : "none"};
  color: ${props =>
    props.movement === "up"
      ? props.theme.colors.black
      : props.movement === "down"
      ? props.theme.colors.black
      : props.theme.colors.fore};
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const BarePriceValue = styled.span.attrs({
  fontSize: 1,
  py: 0,
  m: 0
})`
  cursor: copy;
  &:hover {
    color: ${props => props.theme.colors.link};
  };
  background-color: ${props =>
    props.movement === "up"
      ? props.theme.colors.buy
      : props.movement === "down"
      ? props.theme.colors.sell
      : "none"};
  color: ${props =>
    props.color
      ? props.color
      : props.movement === "up"
      ? props.theme.colors.black
      : props.movement === "down"
      ? props.theme.colors.black
      : props.theme.colors.fore};
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const Container = styled.div`
  ${space};
`

class Price extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = { movement: null }
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.noflash) {
      var movement = null
      if (Number(nextProps.children) > Number(this.props.children)) {
        movement = "up"
      } else if (Number(nextProps.children) < Number(this.props.children)) {
        movement = "down"
      }
      if (movement) {
        clearTimeout(this.timeout)
        this.setState(
          { movement: movement },
          () => (this.timeout = setTimeout(() => this.setState({ movement: null }), 2100))
        )
      }
    }
  }

  componentWillUnmount() {
    if (this.timeout) clearTimeout(this.timeout)
  }

  onClick = () => {
    if (this.props.onClick) {
      this.props.onClick(this.props.children)
    }
  }

  render() {
    if (this.props.hideMissing && (this.props.children === undefined || this.props.children === null)) {
      return null
    }
    const noValue = this.props.noValue ? this.props.noValue : <Loading fitted />
    if (this.props.bare) {
      return (
        <BarePriceValue
          title={this.props.title}
          px={this.props.noflash ? 0 : 1}
          movement={this.state.movement}
          onClick={this.onClick}
          color={this.props.color}
          className={this.props.className}
          data-orko={this.props["data-orko"]}
        >
          {this.props.children === "--" ? "--" : formatNumber(this.props.children, this.props.scale, noValue)}
        </BarePriceValue>
      )
    } else {
      return (
        <Container my={0} mx={2}>
          <PriceKey color={this.props.nameColor ? this.props.nameColor : "fore"} fontSize={1}>
            {this.props.name} {this.props.icon ? <Icon name={this.props.icon} /> : ""}
          </PriceKey>
          <PriceValue
            color={this.props.color ? this.props.color : "heading"}
            fontSize={3}
            movement={this.state.movement}
            onClick={this.onClick}
            title={this.props.title}
            data-orko={this.props["data-orko"]}
          >
            {this.props.children === "--"
              ? "--"
              : formatNumber(this.props.children, this.props.scale, noValue)}
          </PriceValue>
        </Container>
      )
    }
  }
}

const nullOnCLick = () => {}

/**
 * TODO this can be removed now along with the redux connect
 */
function mapStateToProps(state, props) {
  const meta = props.coin ? props.serverApi.coinMetadata.get(props.coin.key) : undefined
  const scale = meta ? meta.priceScale : 8
  return {
    title: props.onClick ? undefined : "Copy price to target field",
    onClick: props.onClick
      ? props.onClick
      : props.frameworkApi.populateLastFocusedField
      ? value => props.frameworkApi.populateLastFocusedField(formatNumber(value, scale, ""))
      : nullOnCLick,
    scale
  }
}

export default withServer(withFramework(connect(mapStateToProps)(Price)))
