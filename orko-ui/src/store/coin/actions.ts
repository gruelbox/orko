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
import * as types from "./actionTypes"

export function setOrderBook(orderBook) {
  return { type: types.SET_ORDERBOOK, payload: orderBook }
}

export function clearUserTrades() {
  return { type: types.CLEAR_USER_TRADES }
}

export function addUserTrade(trade) {
  return { type: types.ADD_USER_TRADE, payload: trade }
}

export function setBalance(exchange: string, currency: string, balance: number) {
  return { type: types.SET_BALANCE, payload: { currency, balance } }
}

export function addTrade(trade) {
  return { type: types.ADD_TRADE, payload: trade }
}

export function clearTrades() {
  return { type: types.CLEAR_TRADES }
}

export function clearBalances() {
  return { type: types.CLEAR_BALANCES }
}

export function orderUpdated(order, timestamp) {
  return { type: types.ORDER_UPDATED, payload: { order, timestamp } }
}

export function clearOrders() {
  return { type: types.CLEAR_ORDERS }
}
