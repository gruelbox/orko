import React from "react"

import styled from "styled-components"
import Price from "./primitives/Price"

const Container = styled.div`
  display: flex;
  > div {
    margin-right: ${props => props.theme.space[2] + "px"};
  }
`

export const Ticker = props => {
  const coin = props.coin
  if (coin) {
    return (
      <Container>
        <Price counter={coin.counter} name="Bid" nameColor="buy" icon="chevron up" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.bid : undefined}
        </Price>
        <Price counter={coin.counter} name="Last" icon="circle outline" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.last : undefined}
        </Price>
        <Price counter={coin.counter} name="Ask" nameColor="sell" icon="chevron down" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.ask : undefined}
        </Price>
        <Price counter={coin.counter} name="Open" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.open : undefined}
        </Price>
        <Price counter={coin.counter} name="24h Low" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.low : undefined}
        </Price>
        <Price counter={coin.counter} name="24h High" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.high : undefined}
        </Price>
      </Container>
    )
  } else {
    return <div>No coin selected</div>
  }
}

export default Ticker
