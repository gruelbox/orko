import * as socketEvents from "./socketEvents"
import * as socketMessages from "./socketMessages"
import ReconnectingWebSocket from "reconnecting-websocket"

var socket
var connected = true

/**
 * Listen to and act on messages from the main thread
 */
self.addEventListener('message', ({data}) => {
  switch (data.eventType) {
    case socketEvents.CONNECT:
      socket = connect(data.payload)
      break
    case socketEvents.DISCONNECT:
      disconnect(socket)
      break
    case socketEvents.MESSAGE:
      send(data.payload)
      break
    default:
      console.log("Unknown message", data)
  }
})

/**
 * Keep the socket alive
 */
setInterval(() => send({command: socketMessages.READY}), 3000)

function send(message) {
  if (connected)
    socket.send(JSON.stringify(message))
}

const messageStart = "{ \"eventType\": \"" + socketEvents.MESSAGE + "\", \"payload\": "

function connect({token, root}) {
  var socket = ws("ws", token, root)
  socket.onopen = () => {
    connected = true
    postMessage("{ \"eventType\": \"" + socketEvents.OPEN + "\" }")
  }
  socket.onclose = () => {
    connected = false
    postMessage("{ \"eventType\": \"" + socketEvents.CLOSE + "\" }")
  }
  socket.onmessage = evt => {
    try {
        postMessage(messageStart + evt.data + "}")
    } catch (e) {
        console.log("Invalid message from server", evt.data)
    }
  }
  return socket
}

function disconnect(socket) {
  socket.close(undefined, "Shutdown", { keepClosed: true })
  connected = false
}

function ws(url, token, root) {
  var fullUrl
  if (root) {
    fullUrl = root + "/" + url
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