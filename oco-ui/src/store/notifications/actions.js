import * as types from './actionTypes';

document.addEventListener('DOMContentLoaded', function () {
  if (!Notification) {
    return;
  }
  if (Notification.permission !== "granted")
    Notification.requestPermission();
});

var lastMessage = null

export function localError(message) {
  return local(message, "ERROR")
}

export function localMessage(message) {
  return local(message, "INFO")
}

export function trace(message) {
  return (dispatch, getState, socket) => {
    dispatch({
      type: types.ADD,
      payload: {
        notificationType: "TRACE",
        message
      }
    })
  }
}

function local(message, level) {
  return (dispatch, getState, socket) => {
    if (lastMessage !== message) {
      notify("OKO Client", message)
      lastMessage = message
    }
    dispatch({
      type: types.ADD,
      payload: {
        notificationType: level,
        message
      }
    })
  }
}

export function add(notification) {
  return (dispatch, getState, socket) => {
    notify("OKO Server", notification.message)
    dispatch({ type: types.ADD, payload: notification })
  }
}

export function clear() {
  return { type: types.CLEAR }
}

function notify(title, message) {
  if (Notification.permission !== "granted")
    Notification.requestPermission()
  else {
    var n = new Notification(title, { body: message })
    setTimeout(n.close.bind(n), 5000);
    n.onclick = () => n.close()
  }
}