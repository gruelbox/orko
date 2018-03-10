import { get } from './fetchUtil';
import { Container } from 'unstated';
import { augmentTicker } from './ticker';

export class PairContainer extends Container {

  constructor() {
    super();
    this.state = {};
  }
  
  fetch = (exchange, auth) => {
    if (!auth.isValid())
      return new Promise(() => (this.state.exchanges));

    return get('exchanges/' + exchange + '/pairs', auth.getUserName(), auth.getPassword())
      .then(response => response.json())
      .then(json => {
        const mapped = json.map(p => augmentTicker(p, exchange));
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
    const pairs = this.state[exchange];
    if (pairs && new Date().getTime() < pairs.ttl) {
      return new Promise(() => (pairs.pairs));
    } else {
      return this.fetch(exchange, auth);
    }
  };
}