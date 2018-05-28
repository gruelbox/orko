import React from "react"
import styled from "styled-components"
import theme from "../theme"
import Price from "../components/primitives/Price"
import { connect } from "react-redux"
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'

const Aligned = styled.div`
  display: flex;
  text-align: ${props => (props.direction === "BID" ? "right" : "left")};
  height: 24px;
  padding-top: 2px;
  padding-bottom: 2px;
`

const Bar = styled.div`
  position: absolute;
  box-shadow: inset 0 6px 8px rgba(0, 0, 0, 0.1);
  top: 0;
  left: ${props => (props.direction === "BID" ? "auto" : 0)};
  right: ${props => (props.direction === "BID" ? 0 : "auto")};
  text-align: ${props => (props.direction === "BID" ? "right" : "left")};
  overflow: hidden;
  color: black;
  font-size: ${props => theme.fontSizes[0] + "px"}
  padding: 0 2px 0 2px;
  height: 100%;
  width: ${props => props.size + "%"};
  -webkit-transition: width 0.5s ease-out;
  transition: width 0.5s ease-out;
  background-color: ${props =>
    props.direction === "BID" ? theme.colors.buy : theme.colors.sell};
`

const BarColumn = styled.div`
  flex-grow: 1;
  position: relative;
  order: ${props => (props.direction === "BID" ? 1 : 2)};
`

const PriceColumn = styled.div`
  flex-basis: auto;
  width: 70px;
  order: ${props => (props.direction === "BID" ? 2 : 1)};
`

const Entry = ({ counter, direction, price, size, focusFn, magnitude }) => (
  <Aligned id={direction + "-" + price} direction={direction}>
    <BarColumn direction={direction}>
      <Bar direction={direction} size={magnitude}>{size}</Bar>
    </BarColumn>
    <PriceColumn direction={direction}>
      <Price
        bare
        noflash
        color={direction === "BID" ? "buy" : "sell"}
        counter={counter}
        onClick={number => {
          if (focusFn) {
            focusFn(number)
          }
        }}
      >
        {price}
      </Price>
    </PriceColumn>
  </Aligned>
)

class OrderBook extends React.PureComponent {
  render() {
    const coin = this.props.coin
    const orders = this.props.orders
    const direction = this.props.direction
    const focusFn = this.props.focusFn
    const largestOrder = Math.max(...orders.map(o => o.remainingAmount))
    return (
      <ReactCSSTransitionGroup
        transitionName="collapse"
        transitionEnter={true}
        transitionAppear={false}
        transitionEnterTimeout={1700}
        transitionLeaveTimeout={1400}
        transitionLeave={true}
      >
        {orders.map(order => (
          <Entry
            key={order.limitPrice}
            focusFn={focusFn}
            counter={coin.counter}
            direction={direction}
            price={order.limitPrice}
            magnitude={order.remainingAmount * 100.0 / largestOrder}
            size={order.remainingAmount}
          />
        ))}
      </ReactCSSTransitionGroup>
    )
  }
}

function mapStateToProps(state) {
  return {
    focusFn: state.focus.fn
  }
}

export default connect(mapStateToProps)(OrderBook)
