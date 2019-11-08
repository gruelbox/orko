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
import { get, put, del, post } from "modules/common/util/fetchUtil"
import { Coin, ServerCoin } from "./Types"

class ExchangesService {
  async fetchSubscriptions(): Promise<Response> {
    return (await get("subscriptions")) as Promise<Response>
  }

  async addSubscription(ticker: ServerCoin): Promise<Response> {
    return (await put("subscriptions", JSON.stringify(ticker))) as Promise<Response>
  }

  async submitOrder(exchange, order) {
    return await post("exchanges/" + exchange + "/orders", JSON.stringify(order))
  }

  async calculateOrder(exchange, order) {
    return await post("exchanges/" + exchange + "/orders/calc", JSON.stringify(order))
  }

  async fetchReferencePrices() {
    return await get("subscriptions/referencePrices")
  }

  async setReferencePrice(coin, price) {
    return await put(
      "subscriptions/referencePrices/" + coin.exchange + "/" + coin.base + "-" + coin.counter,
      price
    )
  }

  async removeSubscription(ticker: ServerCoin): Promise<Response> {
    return (await del("subscriptions", JSON.stringify(ticker))) as Promise<Response>
  }

  async fetchExchanges(): Promise<Response> {
    return (await get("exchanges")) as Promise<Response>
  }

  async fetchPairs(exchange): Promise<Response> {
    return (await get("exchanges/" + exchange + "/pairs")) as Promise<Response>
  }

  async fetchMetadata(coin: Coin): Promise<Response> {
    return (await get("exchanges/" + coin.exchange + "/pairs/" + coin.base + "-" + coin.counter)) as Promise<
      Response
    >
  }

  async fetchTicker(coin) {
    return await get("exchanges/" + coin.exchange + "/markets/" + coin.base + "-" + coin.counter + "/ticker")
  }

  async fetchOrders(coin) {
    return await get("exchanges/" + coin.exchange + "/markets/" + coin.base + "-" + coin.counter + "/orders")
  }

  async cancelOrder(coin: Coin, id: string): Promise<Response> {
    return (await del(
      "exchanges/" + coin.exchange + "/markets/" + coin.base + "-" + coin.counter + "/orders/" + id
    )) as Promise<Response>
  }
}

export default new ExchangesService()
