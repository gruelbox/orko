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

  async cancelOrder(coin, id, orderType) {
    return await del(
      "exchanges/" +
        coin.exchange +
        "/markets/" +
        coin.base +
        "-" +
        coin.counter +
        "/orders/" +
        id +
        "?orderType=" +
        orderType
    )
  }
}

export default new ExchangesService()
