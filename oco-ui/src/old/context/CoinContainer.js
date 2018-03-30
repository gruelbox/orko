import { Container } from 'unstated';
import { Set } from 'immutable';

const LOCAL_STORAGE_KEY = 'CoinContainer.state';

export default class CoinContainer extends Container {

  constructor() {
    super();
    const loaded = localStorage.getItem(LOCAL_STORAGE_KEY);
    if (loaded) {
      this.state = {
        coins: Set(JSON.parse(loaded).coins)
      };
    }
    if (this.state) {
      console.log("Loaded coins", this.state.coins);
    } else {
      this.state = {
        coins: Set()
      };
    } 
  }

  getCoins = () => this.state.coins;

  getByKey = (key) => this.state.coins.find(t => t.key === key);

  addCoin = (coin) => {
    if (this.hasCoin(coin)) {
      console.log("Added existing coin");
      return;
    }
    const newState = {
      coins: this.state.coins.add(coin)
    };
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newState));
    this.setState(newState);
  };

  hasCoin = (coin) => coin && this.state.coins.map(t => t.key).includes(coin.key);

  removeCoin = (coin) => {
    if (!this.hasCoin(coin)) {
      console.log("Removed nonexistent coin");
      return;
    }
    const newState = {
      coins: this.state.coins.filter(t => t.key !== coin.key)
    };
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newState));
    this.setState(newState);
  };
}