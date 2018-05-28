import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const MAX_ITEMS = 50

// TODO should move all this to middleware
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
    case types.CLEAR:
      return Immutable.merge(state, { notifications: [] })
    case types.ADD:
      notify("Server message", action.notification.message)
      const notification = Immutable.set(Immutable(action.notification), "dateTime", new Date())
      var newArr = state.notifications.concat([notification])
      if (newArr.length > MAX_ITEMS) {
        newArr = newArr.slice(1)
      }
      return Immutable.merge(state, { notifications: newArr })
    default:
      return state
  }
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