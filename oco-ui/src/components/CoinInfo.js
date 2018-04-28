import React from "react"

import { Flex, Box } from "rebass"

import Section from "./primitives/Section"
import PriceSet from "./primitives/PriceSet"
import Price from "./primitives/Price"

const EMPTY = "--"

export const CoinInfo = props => {
  const coin = props.coin

  const noBalance =
    !props.balance || !props.balance[coin.base] || !props.balance[coin.counter]

  var coinInfo = (
    <Flex flexWrap="wrap" justifyContent="space-between">
      <Box width={[1 / 2, 1 / 4]}>
        <PriceSet>
          <Price name={coin.base + " total"} onClick={props.onClickNumber}>
            {noBalance ? EMPTY : props.balance[coin.base].total}
          </Price>
          <Price name={coin.counter + " total"} onClick={props.onClickNumber}>
            {noBalance ? EMPTY : props.balance[coin.counter].total}
          </Price>
        </PriceSet>
      </Box>
      <Box width={[1 / 2, 1 / 4]}>
        <PriceSet>
          <Price name={coin.base + " available"} onClick={props.onClickNumber}>
            {noBalance ? EMPTY : props.balance[coin.base].available}
          </Price>
          <Price name={coin.base + " affordable"} onClick={props.onClickNumber}>
            {noBalance || !props.ticker
              ? EMPTY
              : props.balance[coin.counter].available / props.ticker.ask}
          </Price>
          <Price
            name={coin.counter + " available"}
            onClick={props.onClickNumber}
          >
            {noBalance ? EMPTY : props.balance[coin.counter].available}
          </Price>
        </PriceSet>
      </Box>
      <Box width={[1 / 2, 1 / 4]}>
        <PriceSet>
          <Price name="Ask" onClick={props.onClickNumber}>
            {props.ticker ? props.ticker.ask : EMPTY}
          </Price>
          <Price name="Last" onClick={props.onClickNumber}>
            {props.ticker ? props.ticker.last : EMPTY}
          </Price>
          <Price name="Bid" onClick={props.onClickNumber}>
            {props.ticker ? props.ticker.bid : EMPTY}
          </Price>
        </PriceSet>
      </Box>
      <Box width={[1 / 2, 1 / 4]}>
        <PriceSet>
          <Price name="High" onClick={props.onClickNumber}>
            {props.ticker ? props.ticker.high : EMPTY}
          </Price>
          <Price name="Open" onClick={props.onClickNumber}>
            {props.ticker ? props.ticker.open : EMPTY}
          </Price>
          <Price name="Low" onClick={props.onClickNumber}>
            {props.ticker ? props.ticker.low : EMPTY}
          </Price>
        </PriceSet>
      </Box>
    </Flex>
  )

  if (coin) {
    return (
      <Section id="coinInfo" heading={coin.name}>
        {coinInfo}
      </Section>
    )
  } else {
    return <div>No coin selected</div>
  }
}

export default CoinInfo
