import { get, del } from './fetchUtil';

class ExchangesService {
  
  async fetchExchanges(token) {
    return await get('exchanges', token);
  }

  async fetchPairs(exchange, token) {
    return await get('exchanges/' + exchange + '/pairs', token);
  }

  async fetchTicker(coin, token) {
    return await get('exchanges/' + coin.exchange + "/markets/" + coin.base + "-" + coin.counter + "/ticker", token);
  }

  async fetchBalance(coin, token) {
    return await get('exchanges/' + coin.exchange + '/balance/' + coin.base + "," + coin.counter, token);
  }

  async cancelOrder(coin, id, orderType, token) {
    return await del('exchanges/' + coin.exchange + '/markets/' + coin.base + "-" + coin.counter + "/orders/" + id + "?orderType=" +  orderType, token);
  }
}

export default new ExchangesService();