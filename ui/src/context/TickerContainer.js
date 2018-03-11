import { get } from './fetchUtil';
import { Container } from 'unstated';

const DEFAULT_TICKER = { bid: 0, ask: 0 };

export class TickerContainer extends Container {

  firstFetch = true;

  constructor() {
    super();
    this.state = {
      coins: []
    }
  }
  
  fetch = (coin, auth) => {

    if (!auth.isValid())
      return new Promise(() => DEFAULT_TICKER);

    const andAgain = this.firstFetch ? () => {
      this.fetch(coin, auth);
      setTimeout(andAgain, 5000);
    } : () => {};
    this.firstFetch = false;

    const res = 'exchanges/' + coin.exchange + "/markets/" + coin.base + "-" + coin.counter + "/ticker";
    return get(res, auth.getUserName(), auth.getPassword())
      .then(response => response.json())
      .then(json => {
        this.setState({
          [coin.key] : {
            ticker: json,
            ttl: new Date().getTime() + 1000
          }
        });
        setTimeout(andAgain, 2000);
        return json;
      })
      .catch(error => {
        console.log("Failed to fetch ticker", coin);
        var old = this.state[coin.key];
        if (!old)
          old = DEFAULT_TICKER;
        this.setState({
          [coin.key]: {
            ticker: old,
            ttl: new Date().getTime() + 1000
          },
        });
        setTimeout(andAgain, 2000);
        return old;
      })
  };

  getTicker = (coin, auth) => {
    if (!coin)
      return DEFAULT_TICKER;
    const result = this.state[coin.key];
    if (result) {
      if (new Date().getTime() > result.ttl) {
        this.fetch(coin, auth);
      }
      return result.ticker;
    } else {
      this.fetch(coin, auth);
      return DEFAULT_TICKER;
    }
  };
}