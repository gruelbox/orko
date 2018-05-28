import React from "react"

import { connect } from "react-redux"
import { getTopOfOrderBook } from "../selectors/coins"
import OrderBookSide from "./OrderBookSide"
import styled from "styled-components"
import Loading from "../components/primitives/Loading"

const Split = styled.section`
  display: flex;
  flex-direction: row;
  width: 100%;
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
    if ((!this.props.coin && nextProps.coin) || (!this.props.coin.key !== nextProps.coin.key))
      this.largestOrder = 0
  }

  render() {
    const { orderBook, coin } = this.props
    if (orderBook) {
      this.largestOrder = Math.max(
        ...orderBook.bids.map(o => o.remainingAmount),
        ...orderBook.asks.map(o => o.remainingAmount),
        this.largestOrder
      )
    }
    return orderBook ? (
      <Split>
        <AskSide><OrderBookSide key="asks" orders={orderBook.bids} largestOrder={this.largestOrder} direction="BID" coin={coin} /></AskSide>
        <BidSide><OrderBookSide key="buys" orders={orderBook.asks} largestOrder={this.largestOrder} direction="ASK" coin={coin} /></BidSide>
      </Split>
    ) : (
      loading
    )
  }
}

function mapStateToProps(state) {
  return {
    orderBook: getTopOfOrderBook(state)
  }
}

export default connect(mapStateToProps)(OrderBook)