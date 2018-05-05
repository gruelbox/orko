import React from "react"

import styled from "styled-components"
import { Icon } from 'semantic-ui-react';
import { fontSize, color, fontWeight, fontFamily, space } from "styled-system"

/**
 * Rounds a number to a specified number of decimals.
 * 
 * @param {*} value 
 * @param {*} decimals 
 */
function round(value, decimals) {
  return Number(Math.round(value + 'e' + decimals) + 'e-' + decimals);
}

const PriceKey = styled.div.attrs({
  py: 0,
  px: 1,
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
  ${fontFamily}
  ${space}
`

const BarePriceValue = styled.span.attrs({
  fontSize: 1,
  py: 0,
  pl: 1,
  pr: 1,
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
    props.movement === "up"
      ? props.theme.colors.black
      : props.movement === "down"
        ? props.theme.colors.black
        : props.theme.colors.fore};
  ${color}
  ${fontSize}
  ${fontWeight}
  ${fontFamily}
  ${space}
`

const Container = styled.div`
  ${space}
`

class Price extends React.Component {
  constructor(props) {
    super(props)
    this.state = { movement: null }
  }

  componentWillReceiveProps(nextProps) {
    var movement = null
    if (Number(nextProps.children) > Number(this.props.children)) {
      movement = "up"
    } else if (Number(nextProps.children) < Number(this.props.children)) {
      movement = "down"
    }
    if (movement) {
      this.setState({ movement: movement }, () =>
        setTimeout(() => this.setState({ movement: null }), 2100)
      )
    }
  }

  onClick = () => {
    if (this.props.onClick) {
      console.log("Price clicked", this.props.name, this.props.children)
      this.props.onClick(this.props.children)
    }
  }

  render() {
    if (this.props.bare) {
      return (
        <BarePriceValue movement={this.state.movement} onClick={this.onClick}>
          {this.props.children}
        </BarePriceValue>
      )
    } else {
      return (
        <Container m={2}>
          <PriceKey color="fore" fontSize={1}>{this.props.name} {this.props.icon ? <Icon name={this.props.icon}/> : ""}</PriceKey>
          <PriceValue color="heading" fontSize={3} movement={this.state.movement} onClick={this.onClick}>
            {this.props.children 
              ? isNaN(this.props.children)
                ? this.props.children
                : round(this.props.children, 8)
              : ""}
          </PriceValue>
        </Container>
      )
    }
  }
}

export default Price
