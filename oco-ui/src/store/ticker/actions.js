import * as types from "./actionTypes"

/**
 * Connects the socket.
 */
export function connect() {
  return { type: types.CONNECT }
}

/**
 * Shuts down the socket.  It'll attempt to reconnect straight
 * away, but that'll just start failing if you've invalidated
 * the login details.
 */
export function disconnect() {
  return { type: types.DISCONNECT }
}

/**
 * Attempts to reset the subscriptions after an event such as
 * authentication failure or a change in the tracked coins.
 */
export function resubscribe() {
  return { type: types.RESUBSCRIBE }
}