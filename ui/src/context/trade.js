import { put } from './fetchUtil';

export const BUY = 'BUY';
export const SELL = 'SELL';

export function executeTrade(trade, auth) {
  return put('jobs', auth.getUserName(), auth.getPassword(), JSON.stringify(trade))
    .then(response => {
      if (response.status !== 200) {
        throw new Error("Unexpected response from server (" + response.status + ")");
      }
      return response.json()
    });
}

export function createLimitOrder(coin, direction, price, amount) {
  return {
    jobType: "LimitOrderJob",
    tickTrigger: {
      exchange: coin.exchange,
      counter: coin.counter,
      base: coin.base
    },
    direction: direction,
    bigDecimals: {
      amount: amount,
      limitPrice: price
    }
  };
}