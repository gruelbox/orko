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
import React, { useContext, useRef } from "react"

import OrderBookSide from "../components/OrderBookSide"
import styled from "styled-components"
import WhileLoading from "../components/WhileLoading"
import WithCoin from "./WithCoin"
import { SocketContext } from "@orko-ui-socket/index"

const Split = styled.section`
  display: flex;
  flex-direction: row;
  width: 100%;
  background-color: ${props => props.theme.colors.canvas};
  padding: ${props => props.theme.space[1] + "px"};
`

const BidSide = styled.div`
  flex-grow: 1;
  border-left: 1px solid rgba(0, 0, 0, 0.2);
`

const AskSide = styled.div`
  flex-grow: 1;
`

const OrderBookContainer: React.FC<{ animate: boolean }> = ({ animate }) => {
  const orderBook = useContext(SocketContext).orderBook
  const largestOrder = useRef(0)

  return (
    <WithCoin padded>
      {coin => {
        if (orderBook) {
          largestOrder.current = Math.max(
            ...orderBook.bids.map(o => o.remainingAmount),
            ...orderBook.asks.map(o => o.remainingAmount),
            largestOrder.current
          )
        }
        return (
          <WhileLoading data={orderBook} padded>
            {() => (
              <Split>
                <AskSide>
                  <OrderBookSide
                    key="asks"
                    animate={animate}
                    orders={orderBook.bids}
                    largestOrder={largestOrder.current}
                    direction="BID"
                    coin={coin}
                  />
                </AskSide>
                <BidSide>
                  <OrderBookSide
                    key="buys"
                    animate={animate}
                    orders={orderBook.asks}
                    largestOrder={largestOrder.current}
                    direction="ASK"
                    coin={coin}
                  />
                </BidSide>
              </Split>
            )}
          </WhileLoading>
        )
      }}
    </WithCoin>
  )
}

export default OrderBookContainer
