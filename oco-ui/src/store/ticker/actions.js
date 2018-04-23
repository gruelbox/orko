import * as types from "./actionTypes"

/**
 * Attempts to reset the subscriptions after an event such as
 * authentication failure or a change in the tracked coins.
 */
export function resubscribe() {
  return { type: types.RESUBSCRIBE }
}