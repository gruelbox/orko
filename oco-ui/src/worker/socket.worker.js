import * as socketEvents from "./socketEvents"
import ReconnectingWebSocket from "reconnecting-websocket"
import runtimeEnv from "@mars/heroku-js-runtime-env"

var socket

self.addEventListener('message', ({data}) => {
  switch (data.eventType) {
    case socketEvents.CONNECT:
      socket = connect(data.payload)
      break
    case socketEvents.DISCONNECT:
      disconnect(socket)
      break
    case socketEvents.MESSAGE:
      socket.send(JSON.stringify(data.payload))
      break
    default:
      console.log("Unknown message", data)
  }
})

function connect(token) {
  var socket = ws("ws", token)
  socket.onopen = () => postMessage({ eventType: socketEvents.OPEN })
  socket.onclose = () => postMessage({ eventType: socketEvents.CLOSE })
  socket.onmessage = evt => {
    try {
        postMessage({ eventType: socketEvents.MESSAGE, payload: JSON.parse(evt.data) })
    } catch (e) {
        console.log("Invalid message from server", evt.data)
    }
  }
  return socket
}

function disconnect(socket) {
  socket.close(undefined, "Shutdown", { keepClosed: true })
}

function ws(url, token) {
  const env = runtimeEnv()
  var fullUrl
  if (env.REACT_APP_WS_URL) {
    fullUrl = env.REACT_APP_WS_URL + "/" + url
  } else {
    const protocol = self.location.protocol === "https:" ? "wss:" : "ws:"
    fullUrl = protocol + "//" + self.location.host + "/" + url
  }
  console.log("Connecting", fullUrl)
  if (token) {
    return new ReconnectingWebSocket(fullUrl, ["auth", token])
  } else {
    return new ReconnectingWebSocket(fullUrl)
  }
}

export default this