import React from "react"

import styled from "styled-components"
import Price from "./primitives/Price"
import { areEqualShallow } from "../util/objectUtils"

const Container = styled.div`
  display: flex;
  > div {
    margin-right: ${props => props.theme.space[2] + "px"};
  }
`

class Ticker extends React.Component {

  shouldComponentUpdate(nextProps) {
    return !areEqualShallow(this.props, nextProps)
  }

  render() {
    const coin = this.props.coin
    const ticker = this.props.ticker
    const onClickNumber = this.props.onClickNumber
    if (coin) {
      return (
        <Container>
          <Price counter={coin.counter} name="Bid" nameColor="buy" icon="chevron up" onClick={onClickNumber}>
            {ticker ? ticker.bid : undefined}
          </Price>
          <Price counter={coin.counter} name="Last" icon="circle outline" onClick={onClickNumber}>
            {ticker ? ticker.last : undefined}
          </Price>
          <Price counter={coin.counter} name="Ask" nameColor="sell" icon="chevron down" onClick={onClickNumber}>
            {ticker ? ticker.ask : undefined}
          </Price>
          <Price counter={coin.counter} name="Open" onClick={onClickNumber}>
            {ticker ? ticker.open : undefined}
          </Price>
          <Price counter={coin.counter} name="24h Low" onClick={onClickNumber}>
            {ticker ? ticker.low : undefined}
          </Price>
          <Price counter={coin.counter} name="24h High" onClick={onClickNumber}>
            {ticker ? ticker.high : undefined}
          </Price>
        </Container>
      )
    } else {
      return <div>No coin selected</div>
    }
  }
}

export default Ticker
