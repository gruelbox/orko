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
import { createSelector, OutputSelector } from "reselect"
import { getRouterLocation } from "./router"
import { coinFromKey } from "modules/market/coinUtils"
import { Coin } from "modules/market/Types"

export const locationToCoin = (location: Location): Coin => {
  if (
    location &&
    location.pathname &&
    location.pathname.startsWith("/coin/") &&
    location.pathname.length > 6
  ) {
    return coinFromKey(location.pathname.substring(6))
  } else {
    return null
  }
}

export const getSelectedCoin: OutputSelector<any, Coin, (res1: any, res2: any) => Coin> = createSelector(
  [getRouterLocation],
  location => locationToCoin(location)
)
