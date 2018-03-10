import { get } from './fetchUtil';
import { Container } from 'unstated';

export class ExchangeContainer extends Container {

  constructor() {
    super();
    this.state = {
      exchanges: [],
      ttl: 0
    }
  }
  
  fetch = (auth) => {
    if (!auth.isValid())
      return new Promise(() => (this.state.exchanges));

    return get('exchanges', auth.getUserName(), auth.getPassword())
      .then(response => response.json())
      .then(json => {
        this.setState({ 
          exchanges: json,
          ttl: new Date().getTime() + 60000
        });
        return json;
      })
      .catch(error => {
        console.log("Failed to fetch exchanges");
        const old = this.state.exchanges;
        this.setState({
          exchanges: old,
          ttl: new Date().getTime() + 3000
        });
        return old;
      })
  };

  getExchanges = (auth) => {
    if (new Date().getTime() < this.state.ttl) {
      return new Promise(() => (this.state.exchanges));
    } else {
      return this.fetch(auth);
    }
  };
}