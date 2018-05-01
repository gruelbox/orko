import React from "react"

import styled from "styled-components"
import Price from "./primitives/Price"

const EMPTY = "--"

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
        <Price name="Bid" icon="chevron up" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.bid : EMPTY}
        </Price>
        <Price name="Last" icon="circle outline" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.last : EMPTY}
        </Price>
        <Price name="Ask" icon="chevron down" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.ask : EMPTY}
        </Price>
        <Price name="Open" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.open : EMPTY}
        </Price>
        <Price name="24h Low" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.low : EMPTY}
        </Price>
        <Price name="24h High" onClick={props.onClickNumber}>
          {props.ticker ? props.ticker.high : EMPTY}
        </Price>
      </Container>
    )
  } else {
    return <div>No coin selected</div>
  }
}

export default Ticker
