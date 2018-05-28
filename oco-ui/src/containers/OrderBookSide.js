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
  overflow: visible;
`

const Bar = styled.div`
  position: absolute;
  box-shadow: inset 0 6px 8px rgba(0, 0, 0, 0.1);
  top: 0;
  left: ${props => (props.direction === "BID" ? "auto" : 0)};
  right: ${props => (props.direction === "BID" ? 0 : "auto" )};
  height: 100%;
  min-width: 1px;
  width: ${props => props.size + "%"};
  transition: width 0.5s ease-out;
  background-color: ${props => props.direction === "BID" ? theme.colors.buy : theme.colors.sell};
  overflow: visible;
`

const BarSize = styled.div`
  position: absolute;
  top: 0;
  right: ${props => props.direction === "BID"
    ? props.size < 50
      ? "100%"
      : "auto"
    : props.size < 50
      ? "auto"
      : 0};
  left: ${props => props.direction === "BID"
    ? props.size < 50
      ? "auto"
      : 0
    : props.size < 50
      ? "100%"
      : "auto"};
  color: ${props => props.size > 50
    ? "black"
    : props.direction === "BID"
      ? theme.colors.buy
      : theme.colors.sell};
  font-size: ${props => theme.fontSizes[0] + "px"}
  padding: 0 4px 0 4px;
  overflow: visible;
  white-space: nowrap;
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
      <Bar direction={direction} size={magnitude}>
        <BarSize direction={direction} size={magnitude}>{size}</BarSize>
      </Bar>
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

const OrderBookSide = ({coin, orders, direction, focusFn, largestOrder}) => (
  <ReactCSSTransitionGroup
    transitionName="collapse"
    transitionEnter={true}
    transitionAppear={false}
    transitionEnterTimeout={2000}
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

function mapStateToProps(state) {
  return {
    focusFn: state.focus.fn
  }
}

export default connect(mapStateToProps)(OrderBookSide)
