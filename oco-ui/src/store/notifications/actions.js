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
  return (dispatch, getState, socket) => {
    if (lastMessage !== message) {
      notify("Error", message)
      lastMessage = message
    }
    dispatch({
      type: types.ADD,
      payload: {
        notificationType: "ERROR",
        message
      }
    })
  }
}

export function add(notification) {
  return (dispatch, getState, socket) => {
    notify("Server message", notification.message)
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