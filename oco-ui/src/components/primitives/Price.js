import React from "react"
import { Icon } from "semantic-ui-react"
import styled from "styled-components"
import { fontSize, color, fontWeight, space } from "styled-system"
import { isValidNumber } from "../../util/numberUtils"
import Loading from "./Loading"

/**
 * Formatters for amounts in specific counter currencies.
 */
const formatters = {
  "BTC": x => (isValidNumber(x) ? Number(x).toFixed(8) : <Loading fitted />),
  "ETH": x => (isValidNumber(x) ? Number(x).toFixed(7) : <Loading fitted />),
  "USDT": x => (isValidNumber(x) ? Number(x).toFixed(2) : <Loading fitted />),
  "USD": x => (isValidNumber(x) ? Number(x).toFixed(2) : <Loading fitted />),
  "EUR": x => (isValidNumber(x) ? Number(x).toFixed(2) : <Loading fitted />),
  "XXX": x => (isValidNumber(x) ? x : <Loading fitted />)
}

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
  ${space}
`

const Container = styled.div`
  ${space};
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
    var formatter = formatters[this.props.counter]
      if (!formatter) {
        formatter = formatters["XXX"]
      }
    if (this.props.bare) {
      return (
        <BarePriceValue movement={this.state.movement} onClick={this.onClick}>
          {this.props.children === "--" ? "--" : formatter(this.props.children)}
        </BarePriceValue>
      )
    } else {
      return (
        <Container m={2}>
          <PriceKey
            color={this.props.nameColor ? this.props.nameColor : "fore"}
            fontSize={1}
          >
            {this.props.name}{" "}
            {this.props.icon ? <Icon name={this.props.icon} /> : ""}
          </PriceKey>
          <PriceValue
            color="heading"
            fontSize={3}
            movement={this.state.movement}
            onClick={this.onClick}
          >
            {this.props.children === "--" ? "--" : formatter(this.props.children)}
          </PriceValue>
        </Container>
      )
    }
  }
}

export default Price
