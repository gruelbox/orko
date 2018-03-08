import React from 'react';
import createReactContext from 'create-react-context';
import { get } from './fetchUtil';
import { AuthConsumer } from './AuthContext';

const TickerContext = createReactContext('tickers')

function tickerName(spec) {
  return spec.exchange + "-" + spec.base + "-" + spec.counter
}

const defaultTicker = {
  ask: undefined,
  bid: undefined
};

export const TickerConsumer = TickerContext.Consumer;

export class TickerProvider extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      tickers: [],
      exchanges: []
    }
  }

  render() {
    const self = this;
    const content = <TickerContext.Provider value={this.state}>
                      {this.props.children}
                    </TickerContext.Provider>;
    return (
      <AuthConsumer>{ auth => {
        if (auth.valid && this.state.exchanges.length === 0) {
          get('exchanges', auth.userName, auth.password)
            .then(response => response.json())
            .then(json => {
              self.setState({ exchanges: json });
              return json;
            })
            .catch(error => {
              console.log("Failed to fetch exchanges");
            });;
        }

        return content;

        /* if (!this.state.tickers) {
          const name = tickerName(spec);
          return get('exchanges/' + spec.exchange + '/markets/' + spec.base + "-" + spec.counter + "/ticker", auth.userName, auth.password)
            .then(response => response.json())
            .then(json => {
              const result = {
                ticker: json,
                lastFetch: new Date()
              };
              self.setState(prev => {
                tickers: prev.tickers.set(name, result);
              });
              return json;
            });
        } */
        
      }}</AuthConsumer>
    )
  }
}