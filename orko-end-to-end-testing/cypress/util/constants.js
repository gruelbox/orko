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
export const IP_WHITELISTING_SECRET = "KJY4B3ZOFWRNNCN4"
export const IP_WHITELISTING_SECRET_INVALID = "KJY4B3ZOFWRNNCN3"
export const LOGIN_SECRET = "O546XLKJMJIQM3PW"
export const LOGIN_SECRET_INVALID = "O546XLKJMJIQM3PX"
export const LOGIN_USER = "ci"
export const LOGIN_PW = "tester"

export const PERCENT_CHANGE_REGEX = /-?[0-9]\d*(\.\d+)?%/
export const NUMBER_REGEX = /-?[0-9]\d*(\.\d+)?/
export const LONG_WAIT = 60000

export const BINANCE_ETH = {
  exchange: "binance",
  counter: "USDT",
  base: "ETH"
}

export const BINANCE_BTC = {
  exchange: "binance",
  counter: "USDT",
  base: "BTC"
}
