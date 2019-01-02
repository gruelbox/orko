/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
import * as types from "./actionTypes"

document.addEventListener("DOMContentLoaded", function() {
  if (!Notification) {
    return
  }
  if (Notification.permission !== "granted") Notification.requestPermission()
})

var lastMessage = null

export function localError(message) {
  return local(message, "ERROR")
}

export function localAlert(message) {
  return local(message, "ALERT")
}

export function localMessage(message) {
  return local(message, "INFO")
}

export function trace(message) {
  return (dispatch, getState, socket) => {
    dispatch({
      type: types.ADD,
      payload: {
        level: "TRACE",
        message
      }
    })
  }
}

function local(message, level) {
  return (dispatch, getState, socket) => {
    if (lastMessage !== message) {
      if (level === "ALERT" || level === "ERROR") {
        notify("Orko Client", message)
      }
      lastMessage = message
    }
    dispatch({
      type: types.ADD,
      payload: {
        level: level,
        message
      }
    })
  }
}

export function add(notification) {
  return (dispatch, getState, socket) => {
    if (notification.level === "ALERT" || notification.level === "ERROR") {
      notify("Orko Server", notification.message)
    }
    dispatch({ type: types.ADD, payload: notification })
  }
}

export function addStatusCallback(requestId, callback) {
  return (dispatch, getState, socket) => {
    const registration = getState().notifications.statusCallbacks[requestId]
    if (registration) {
      if (callback) callback(registration.status)
      dispatch({ type: types.COMPLETE_CALLBACK, payload: requestId })
    } else {
      dispatch({
        type: types.REQUEST_CALLBACK,
        payload: { requestId, callback }
      })
    }
  }
}

export function statusUpdate(update) {
  return (dispatch, getState, socket) => {
    const registration = getState().notifications.statusCallbacks[
      update.requestId
    ]
    if (registration) {
      if (registration.callback) registration.callback(update)
      dispatch({ type: types.COMPLETE_CALLBACK, payload: update.requestId })
    } else {
      dispatch({ type: types.DEFER_CALLBACK, payload: update })
    }
  }
}

export function clear() {
  return { type: types.CLEAR }
}

function notify(title, message) {
  if (Notification.permission !== "granted") Notification.requestPermission()
  else {
    var n = new Notification(title, { body: message })
    setTimeout(n.close.bind(n), 5000)
    n.onclick = () => n.close()
  }
}
