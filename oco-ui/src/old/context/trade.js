import { put } from './fetchUtil';

export const BUY = 'BUY';
export const SELL = 'SELL';

export function executeTrade(trade, auth) {
  return put('jobs', auth.userName, auth.password, JSON.stringify(trade))
    .then(auth.parseToJson);
}