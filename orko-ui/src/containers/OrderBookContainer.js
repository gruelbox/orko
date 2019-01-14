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

import { connect } from "react-redux"
import { getTopOfOrderBook, getSelectedCoin } from "../selectors/coins"
import OrderBookSide from "../components/OrderBookSide"
import styled from "styled-components"
import WhileLoading from "../components/WhileLoading"

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

class OrderBookContainer extends React.PureComponent {
  constructor(props) {
    super(props)
    this.largestOrder = 0
  }

  componentWillReceiveProps(nextProps) {
    if (
      !this.props.coin ||
      !nextProps.coin ||
      !this.props.coin.key !== nextProps.coin.key
    )
      this.largestOrder = 0
  }

  render() {
    const { orderBook, coin, animate } = this.props
    const haveData = orderBook && coin
    if (haveData) {
      this.largestOrder = Math.max(
        ...orderBook.bids.map(o => o.remainingAmount),
        ...orderBook.asks.map(o => o.remainingAmount),
        this.largestOrder
      )
    }
    return (
      <WhileLoading data={haveData} padded>
        {() => (
          <Split>
            <AskSide>
              <OrderBookSide
                key="asks"
                animate={animate}
                orders={orderBook.bids}
                largestOrder={this.largestOrder}
                direction="BID"
                coin={coin}
              />
            </AskSide>
            <BidSide>
              <OrderBookSide
                key="buys"
                animate={animate}
                orders={orderBook.asks}
                largestOrder={this.largestOrder}
                direction="ASK"
                coin={coin}
              />
            </BidSide>
          </Split>
        )}
      </WhileLoading>
    )
  }
}

export default connect(state => ({
  orderBook: getTopOfOrderBook(state),
  coin: getSelectedCoin(state)
}))(OrderBookContainer)
