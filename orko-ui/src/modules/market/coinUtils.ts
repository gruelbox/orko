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
import Immutable from "seamless-immutable"
import { PartialServerCoin, ServerCoin, Coin } from "./Types"

export function coin(exchange: string, counter: string, base: string): Coin {
  return augmentCoin(
    {
      counter: counter,
      base: base
    },
    exchange
  )
}

export function coinFromKey(key: string): Coin {
  const split = key.split("/")
  return augmentCoin(
    {
      counter: split[1],
      base: split[2]
    },
    split[0]
  )
}

export function coinFromTicker(t: ServerCoin): Coin {
  return augmentCoin(t, t.exchange)
}

export function tickerFromCoin(coin: Coin): ServerCoin {
  return {
    counter: coin.counter,
    base: coin.base,
    exchange: coin.exchange
  }
}

export function augmentCoin(p: ServerCoin | PartialServerCoin, exchange?: string): Coin {
  return Immutable.merge(p, {
    exchange: exchange ? exchange : (p as ServerCoin).exchange,
    key: (exchange ? exchange : (p as ServerCoin).exchange) + "/" + p.counter + "/" + p.base,
    name: p.base + "/" + p.counter + " (" + exchange + ")",
    shortName: p.base + "/" + p.counter
  })
}
