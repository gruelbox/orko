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
import Amount from "./primitives/Amount"
import Para from "./primitives/Para"

const Container = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
`

const LOG_10 = Math.log(10)
const log10 = x => Math.log(1 / x) / LOG_10
const scaleOfValue = x => Math.floor(log10(x))

export const Balance = ({ coin, balance, ticker, onClickNumber }) => {
  const noBalances = !balance || !coin
  const noTicker = !ticker

  const noBaseBalance = noBalances || (!balance.get(coin.base) && balance.get(coin.base) !== 0)
  const noCounterBalance = noBalances || (!balance.get(coin.counter) && balance.get(coin.counter) !== 0)

  const counterScale = meta => (meta.minimumAmount ? scaleOfValue(meta.minimumAmount) : 8)

  const baseBalance = noBalances ? null : balance.get(coin.base)
  const baseCounter = noBalances ? null : balance.get(coin.counter)

  if (coin) {
    return (
      <Container>
        <Amount name="Balance" fontSize={1} onClick={onClickNumber} coin={coin}>
          {noBaseBalance ? undefined : baseBalance.total}
        </Amount>
        <Amount name="Can sell" fontSize={1} onClick={onClickNumber} coin={coin}>
          {noBaseBalance ? undefined : baseBalance.available}
        </Amount>
        <Amount name="Sale value at bid" fontSize={1} onClick={onClickNumber} coin={coin}>
          {noBaseBalance || noTicker ? undefined : +Number(baseBalance.total * ticker.bid).toFixed(8)}
        </Amount>
        <Amount name={coin.counter + " balance"} fontSize={1} onClick={onClickNumber} coin={coin}>
          {noCounterBalance ? undefined : baseCounter.total}
        </Amount>
        <Amount name={coin.counter + " available"} fontSize={1} onClick={onClickNumber} coin={coin}>
          {noCounterBalance ? undefined : baseCounter.available}
        </Amount>
        <Amount name="Can buy" deriveScale={counterScale} fontSize={1} onClick={onClickNumber} coin={coin}>
          {noCounterBalance || noTicker ? undefined : baseCounter.available / ticker.ask}
        </Amount>
      </Container>
    )
  } else {
    return <Para>No coin selected</Para>
  }
}

export default Balance
