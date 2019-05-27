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
import { get, put, del, post } from "./fetchUtil"

class ExchangesService {
  async fetchSubscriptions() {
    return await get("subscriptions")
  }

  async addSubscription(ticker) {
    return await put("subscriptions", ticker)
  }

  async submitOrder(exchange, order) {
    return await post(
      "exchanges/" + exchange + "/orders",
      JSON.stringify(order)
    )
  }

  async calculateOrder(exchange, order) {
    return await post(
      "exchanges/" + exchange + "/orders/calc",
      JSON.stringify(order)
    )
  }

  async fetchReferencePrices() {
    return await get("subscriptions/referencePrices")
  }

  async setReferencePrice(coin, price) {
    return await put(
      "subscriptions/referencePrices/" +
        coin.exchange +
        "/" +
        coin.base +
        "-" +
        coin.counter,
      price
    )
  }

  async removeSubscription(ticker) {
    return await del("subscriptions", ticker)
  }

  async fetchExchanges() {
    return await get("exchanges")
  }

  async fetchPairs(exchange) {
    return await get("exchanges/" + exchange + "/pairs")
  }

  async fetchMetadata(coin) {
    return await get(
      "exchanges/" + coin.exchange + "/pairs/" + coin.base + "-" + coin.counter
    )
  }

  async fetchTicker(coin) {
    return await get(
      "exchanges/" +
        coin.exchange +
        "/markets/" +
        coin.base +
        "-" +
        coin.counter +
        "/ticker"
    )
  }

  async fetchOrders(coin) {
    return await get(
      "exchanges/" +
        coin.exchange +
        "/markets/" +
        coin.base +
        "-" +
        coin.counter +
        "/orders"
    )
  }

  async fetchBalance(coin) {
    return await get(
      "exchanges/" +
        coin.exchange +
        "/balance/" +
        coin.base +
        "," +
        coin.counter
    )
  }

  async cancelOrder(coin, id) {
    return await del(
      "exchanges/" +
        coin.exchange +
        "/markets/" +
        coin.base +
        "-" +
        coin.counter +
        "/orders/" +
        id
    )
  }
}

export default new ExchangesService()
