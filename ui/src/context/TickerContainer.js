import { Container } from 'unstated';
import { Set } from 'immutable';

const LOCAL_STORAGE_KEY = 'TickerContainer.state';

export class TickerContainer extends Container {

  constructor() {
    super();
    const loaded = localStorage.getItem(LOCAL_STORAGE_KEY);
    if (loaded) {
      this.state = {
        tickers: Set(JSON.parse(loaded).tickers)
      };
    }
    if (this.state) {
      console.log("Loaded tickers", this.state.tickers);
    } else {
      this.state = {
        tickers: Set()
      };
    } 
  }

  getTickers = () => this.state.tickers;

  getByKey = (key) => this.state.tickers.find(t => t.key === key);

  addTicker = (ticker) => {
    if (this.hasTicker(ticker)) {
      console.log("Added existing ticker");
      return;
    }
    const newState = {
      tickers: this.state.tickers.add(ticker)
    };
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newState));
    this.setState(newState);
  };

  hasTicker = (ticker) => this.state.tickers.map(t => t.key).includes(ticker.key);

  removeTicker = (ticker) => {
    if (!this.hasTicker(ticker)) {
      console.log("Removed nonexistent ticker");
      return;
    }
    const newState = {
      tickers: this.state.tickers.filter(t => t.key !== ticker.key)
    };
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(newState));
    this.setState(newState);
  };
}