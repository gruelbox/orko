import React from "react"
import styled from "styled-components"
import Price from "./primitives/Price"
import Para from "./primitives/Para"

const Container = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
`

export const Balance = props => {
  const coin = props.coin

  const noBalances =
    !props.balance ||
    !props.coin

  const noBaseBalance = noBalances || (!props.balance[coin.base] && props.balance[coin.base] !== 0)
  const noCounterBalance = noBalances || (!props.balance[coin.counter] && props.balance[coin.counter] !== 0)

  if (coin) {
    return (
      <Container>
        <Price fontSize={1} name="Total" onClick={props.onClickNumber}>
          {noBaseBalance ? undefined : props.balance[coin.base].total}
        </Price>
        <Price fontSize={1} name="Available" onClick={props.onClickNumber}>
          {noBaseBalance ? undefined : props.balance[coin.base].available}
        </Price>
        <Price
          fontSize={1}
          counter={coin.counter}
          name={coin.counter + " total"}
          onClick={props.onClickNumber}
        >
          {noCounterBalance ? undefined : props.balance[coin.counter].total}
        </Price>
        <Price
          fontSize={1}
          counter={coin.counter}
          name={coin.counter + " available"}
          onClick={props.onClickNumber}
        >
          {noCounterBalance ? undefined : props.balance[coin.counter].available}
        </Price>
        <Price fontSize={1} name="Can buy" onClick={props.onClickNumber}>
          {noCounterBalance || !props.ticker
            ? undefined
            : props.balance[coin.counter].available / props.ticker.ask}
        </Price>
      </Container>
    )
  } else {
    return <Para>No coin selected</Para>
  }
}

export default Balance