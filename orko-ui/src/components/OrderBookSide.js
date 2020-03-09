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
import theme from "../theme"
import Price from "../components/primitives/Price"
import ReactCSSTransitionGroup from "react-addons-css-transition-group"
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
    .orderbook-value {
      color: ${props => lighten(0.2, props.direction === "BID" ? theme.colors.buy : theme.colors.sell)};
    }
    .orderbook-bar {
      background-color: ${props =>
    lighten(0.2, props.direction === "BID" ? theme.colors.buy : theme.colors.sell)};
    }
    &.collapse-enter-active {
      .orderbook-value {
        color: ${props => (props.direction === "BID" ? theme.colors.buy : theme.colors.sell)};
        transition: color 200ms ease-in 200ms;
      }
      .orderbook-bar {
        background-color: ${props => (props.direction === "BID" ? theme.colors.buy : theme.colors.sell)};
        transition: background-color 200ms ease-in 200ms;
      }
    }
  }

  &.collapse-leave {
    opacity: 0.4;
  }
`

const Bar = styled.div`
  position: absolute;
  box-shadow: inset 0 6px 8px rgba(0, 0, 0, 0.1);
  top: 0;
  left: ${props => (props.direction === "BID" ? "auto" : 0)};
  right: ${props => (props.direction === "BID" ? 0 : "auto")};
  height: 100%;
  min-width: 1px;
  width: ${props => props.size + "%"};
  transition: width 0.3s ease-out;
  background-color: ${props => (props.direction === "BID" ? theme.colors.buy : theme.colors.sell)};
  overflow: visible;
`

const BarSize = styled.div`
  position: absolute;
  top: -1px;
  right: ${props =>
    props.direction === "BID" ? (props.size < 50 ? "100%" : "auto") : props.size < 50 ? "auto" : 0};
  left: ${props =>
    props.direction === "BID" ? (props.size < 50 ? "auto" : 0) : props.size < 50 ? "100%" : "auto"};
  color: ${props =>
    props.size > 50 ? "black" : props.direction === "BID" ? theme.colors.buy : theme.colors.sell};
  font-size: ${props => theme.fontSizes[0] + "px"};
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
  padding-left: ${props => theme.space[1] + "px"};
  padding-right: ${props => theme.space[1] + "px"};
`

const Entry = ({ coin, direction, price, size, focusFn, magnitude }) => (
  <Aligned id={direction + "-" + price} direction={direction}>
    <BarColumn direction={direction}>
      <Bar direction={direction} size={magnitude} className="orderbook-bar">
        <BarSize direction={direction} size={magnitude} className="orderbook-value">
          {size}
        </BarSize>
      </Bar>
    </BarColumn>
    <PriceColumn direction={direction}>
      <Price
        bare
        noflash
        className="orderbook-value"
        color={direction === "BID" ? "buy" : "sell"}
        coin={coin}
      >
        {price}
      </Price>
    </PriceColumn>
  </Aligned>
)

const entries = ({ coin, orders, direction, focusFn, largestOrder }) =>
  orders.map(order => (
    <Entry
      key={order.limitPrice}
      focusFn={focusFn}
      coin={coin}
      direction={direction}
      price={order.limitPrice}
      magnitude={(order.remainingAmount * 100.0) / largestOrder}
      size={order.remainingAmount}
    />
  ))

const OrderBookSide = props =>
  props.animate ? (
    <ReactCSSTransitionGroup
      transitionName="collapse"
      transitionEnter={true}
      transitionAppear={false}
      transitionEnterTimeout={1600}
      transitionLeaveTimeout={1000}
      transitionLeave={true}
    >
      {entries(props)}
    </ReactCSSTransitionGroup>
  ) : (
      <div>{entries(props)}</div>
    )

export default OrderBookSide
