import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const MAX_ITEMS = 50

document.addEventListener('DOMContentLoaded', function () {
  if (!Notification) {
    return;
  }
  if (Notification.permission !== "granted")
    Notification.requestPermission();
});

const initialState = Immutable({
  notifications: Immutable([])
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.ADD:
      console.debug(action.type, action)
      notify("Server event", action.notification.message)
      var newArr = state.notifications.concat(action.notification)
      if (state.notifications.length > MAX_ITEMS) {
        newArr = newArr.slice(1)
      }
      return Immutable({ notifications: newArr })
    default:
      return state
  }
}

function notify(title, message) {
  if (Notification.permission !== "granted")
    Notification.requestPermission();
  else {
    new Notification(title, {
      body: message,
    });
  }
}