import { get, put, del } from "./fetchUtil"

class ExchangesService {
  async fetchSubscriptions(token) {
    return await get("subscriptions", token)
  }

  async addSubscription(token, ticker) {
    return await put("subscriptions", token, ticker)
  }

  async fetchReferencePrices(token) {
    return await get("subscriptions/referencePrices", token)
  }

  async setReferencePrice(token, coin, price) {
    return await put(
      "subscriptions/referencePrices/" +
        coin.exchange +
        "/" +
        coin.base +
        "-" +
        coin.counter,
      token,
      price
    )
  }

  async removeSubscription(token, ticker) {
    return await del("subscriptions", token, ticker)
  }

  async fetchExchanges(token) {
    return await get("exchanges", token)
  }

  async fetchPairs(exchange, token) {
    return await get("exchanges/" + exchange + "/pairs", token)
  }

  async fetchMetadata(coin, token) {
    return await get(
      "exchanges/" + coin.exchange + "/pairs/" + coin.base + "-" + coin.counter,
      token
    )
  }

  async fetchTicker(coin, token) {
    return await get(
      "exchanges/" +
        coin.exchange +
        "/markets/" +
        coin.base +
        "-" +
        coin.counter +
        "/ticker",
      token
    )
  }

  async fetchOrders(coin, token) {
    return await get(
      "exchanges/" +
        coin.exchange +
        "/markets/" +
        coin.base +
        "-" +
        coin.counter +
        "/orders",
      token
    )
  }

  async fetchBalance(coin, token) {
    return await get(
      "exchanges/" +
        coin.exchange +
        "/balance/" +
        coin.base +
        "," +
        coin.counter,
      token
    )
  }

  async cancelOrder(coin, id, orderType, token) {
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
        orderType,
      token
    )
  }
}

export default new ExchangesService()
