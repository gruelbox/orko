import { get } from './fetchUtil';

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

}

export default new ExchangesService();