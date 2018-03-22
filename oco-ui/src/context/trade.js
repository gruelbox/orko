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