import { get } from './fetchUtil';
import { Container } from 'unstated';
import { augmentCoin } from './coin';

export default class PairContainer extends Container {

  constructor() {
    super();
    this.state = {};
  }
  
  fetch = (exchange, auth) => {
    if (!auth.valid) {
      return new Promise(() => []);
    }

    return get('exchanges/' + exchange + '/pairs', auth.userName, auth.password)
      .then(auth.parseToJson)
      .then(json => {
        const mapped = json.map(p => augmentCoin(p, exchange));
        this.setState({ 
          [exchange]: {
            pairs: mapped,
            ttl: new Date().getTime() + 60000
          },
        });
        return mapped;
      })
      .catch(error => {
        console.log("Failed to fetch pairs for exchange", exchange);
        var old = this.state[exchange];
        if (!old)
          old = [];
        this.setState({
          [exchange]: {
            pairs: old,
            ttl: new Date().getTime() + 3000
          },
        });
        return old;
      })
  };

  getPairs = (exchange, auth) => {
    if (!exchange)
      return [];
    const result = this.state[exchange];
    if (result) {
      if (new Date().getTime() > result.ttl) {
        this.fetch(exchange, auth);
      }
      return result.pairs;
    } else {
      this.fetch(exchange, auth);
      return [];
    }
  };
}