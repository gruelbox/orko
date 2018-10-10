import React from "react"
import { Icon } from "semantic-ui-react"
import styled from "styled-components"
import { fontSize, color, fontWeight, space } from "styled-system"
import { formatNumber } from "../../util/numberUtils"
import Loading from "./Loading"
import { connect } from "react-redux"

const AmountKey = styled.div.attrs({
  py: 0,
  px: 1
})`
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`

const AmountValue = styled.div`
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

const BareAmountValue = styled.span.attrs({
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

class Amount extends React.PureComponent {
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
        this.setState(
          { movement: movement },
          () =>
            (this.timeout = setTimeout(
              () => this.setState({ movement: null }),
              2100
            ))
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
    if (this.props.bare) {
      return (
        <BareAmountValue
          title={this.props.title}
          px={this.props.noflash ? 0 : 1}
          movement={this.state.movement}
          onClick={this.onClick}
          color={this.props.color}
          className={this.props.className}
        >
          {this.props.children === "--"
            ? "--"
            : formatNumber(
                this.props.children,
                this.props.meta ? this.props.meta.amountScale : 8,
                <Loading fitted />
              )}
        </BareAmountValue>
      )
    } else {
      return (
        <Container my={0} mx={2}>
          <AmountKey
            color={this.props.nameColor ? this.props.nameColor : "fore"}
            fontSize={1}
          >
            {this.props.name}{" "}
            {this.props.icon ? <Icon name={this.props.icon} /> : ""}
          </AmountKey>
          <AmountValue
            color={this.props.color ? this.props.color : "heading"}
            fontSize={3}
            movement={this.state.movement}
            onClick={this.onClick}
            title={this.props.title}
          >
            {this.props.children === "--"
              ? "--"
              : formatNumber(
                  this.props.children,
                  this.props.meta ? this.props.meta.amountScale : 8,
                  <Loading fitted />
                )}
          </AmountValue>
        </Container>
      )
    }
  }
}

const nullOnCLick = number => {}

function mapStateToProps(state, props) {
  const meta = props.coin ? state.coins.meta[props.coin.key] : undefined
  return {
    title: props.onClick ? undefined : "Copy amount to target field",
    onClick: props.onClick
      ? props.onClick
      : state.focus.fn
        ? value =>
            state.focus.fn(formatNumber(value, meta ? meta.amountScale : 8, ""))
        : nullOnCLick,
    meta
  }
}

export default connect(mapStateToProps)(Amount)
