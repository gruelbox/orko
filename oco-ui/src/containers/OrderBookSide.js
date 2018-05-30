import React from "react"
import styled from "styled-components"
import theme from "../theme"
import Price from "../components/primitives/Price"
import { connect } from "react-redux"
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'
import { lighten } from "polished"

const Aligned = styled.div`
  display: flex;
  text-align: ${props => (props.direction === "BID" ? "right" : "left")};
  height: 24px;
  padding-top: 3px;
  border-bottom: 1px solid ${props => props.theme.colors.backgrounds[3]};
  padding-bottom: 3px;
  overflow: visible;

  &.collapse-enter {
    opacity: 0.1;
    .orderbook-value {
      color: ${props => lighten(0.2, props.direction === "BID" ? theme.colors.buy : theme.colors.sell)};
    }
    .orderbook-bar {
      background-color: ${props => lighten(0.2, props.direction === "BID" ? theme.colors.buy : theme.colors.sell)};
    }
    &.collapse-enter-active {
      opacity: 1;
      transition: opacity 400ms ease-in 300ms;
      .orderbook-value {
        color: ${props => props.direction === "BID" ? theme.colors.buy : theme.colors.sell};
        transition: color 400ms ease-in 1400ms;
      }
      .orderbook-bar {
        background-color: ${props => props.direction === "BID" ? theme.colors.buy : theme.colors.sell};
        transition: background-color 400ms ease-in 1400ms;
      }
    }
  }

  &.collapse-leave {
    height: 24px !important;
    opacity: 0.4;
    &.collapse-leave-active {
      opacity: 0;
      height: 0px !important;
      transition: height 400ms ease-in 1000ms, opacity 400ms linear 1000ms;
    }
  }
  
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
  top: -1px;
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
      <Bar direction={direction} size={magnitude} className="orderbook-bar">
        <BarSize direction={direction} size={magnitude} className="orderbook-value">{size}</BarSize>
      </Bar>
    </BarColumn>
    <PriceColumn direction={direction}>
      <Price
        bare
        noflash
        className="orderbook-value"
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
    transitionEnterTimeout={1800}
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
