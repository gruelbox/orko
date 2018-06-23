import React from "react"

import { connect } from "react-redux"
import { getTopOfOrderBook } from "../selectors/coins"
import OrderBookSide from "../components/OrderBookSide"
import styled from "styled-components"
import Loading from "../components/primitives/Loading"

const Split = styled.section`
  display: flex;
  flex-direction: row;
  width: 100%;
  background-color: ${props => props.theme.colors.canvas};
  padding: ${props => props.theme.space[1] + "px"}
`

const BidSide = styled.div`
  flex-grow: 1;
  border-left: 1px solid rgba(0,0,0,0.2);
`

const AskSide = styled.div`
  flex-grow: 1;
`

const loading = <Loading p={2} />

class OrderBook extends React.PureComponent {

  constructor(props) {
    super(props)
    this.largestOrder = 0
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.coin || !nextProps.coin || (!this.props.coin.key !== nextProps.coin.key))
      this.largestOrder = 0
  }

  render() {
    const { orderBook, coin, animate } = this.props
    if (orderBook && coin) {
      this.largestOrder = Math.max(
        ...orderBook.bids.map(o => o.remainingAmount),
        ...orderBook.asks.map(o => o.remainingAmount),
        this.largestOrder
      )
    }
    return (orderBook && coin) ? (
      <Split>
        <AskSide><OrderBookSide key="asks" animate={animate} orders={orderBook.bids} largestOrder={this.largestOrder} direction="BID" coin={coin} /></AskSide>
        <BidSide><OrderBookSide key="buys" animate={animate} orders={orderBook.asks} largestOrder={this.largestOrder} direction="ASK" coin={coin} /></BidSide>
      </Split>
    ) : (
      loading
    )
  }
}

export default connect(state => ({
  orderBook: getTopOfOrderBook(state)
}))(OrderBook)