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
import Price from "./primitives/Price"
import { areEqualShallow } from "modules/common/util/objectUtils"

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
    const { coin, ticker, onClickNumber, mobile } = this.props
    if (coin) {
      return (
        <Container>
          {!mobile && (
            <Price
              coin={coin}
              name="Bid"
              nameColor="buy"
              icon="chevron up"
              onClick={onClickNumber}
            >
              {ticker ? ticker.bid : undefined}
            </Price>
          )}

          <Price
            coin={coin}
            name="Last"
            icon="circle outline"
            onClick={onClickNumber}
          >
            {ticker ? ticker.last : undefined}
          </Price>
          {!mobile && (
            <>
              <Price
                coin={coin}
                name="Ask"
                nameColor="sell"
                icon="chevron down"
                onClick={onClickNumber}
              >
                {ticker ? ticker.ask : undefined}
              </Price>
              <Price
                coin={coin}
                name="Open"
                onClick={onClickNumber}
                hideMissing
              >
                {ticker ? ticker.open : undefined}
              </Price>
              <Price
                coin={coin}
                name="24h Low"
                onClick={onClickNumber}
                hideMissing
              >
                {ticker ? ticker.low : undefined}
              </Price>
              <Price
                coin={coin}
                name="24h High"
                onClick={onClickNumber}
                hideMissing
              >
                {ticker ? ticker.high : undefined}
              </Price>
            </>
          )}
        </Container>
      )
    } else {
      return <div>No coin selected</div>
    }
  }
}

export default Ticker
