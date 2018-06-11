import * as socketEvents from "./socketEvents"
import * as socketMessages from "./socketMessages"
import ReconnectingWebSocket from "reconnecting-websocket"

var socket
var connected = true
var timer

/**
 * Listen to and act on messages from the main thread
 */
self.addEventListener('message', ({data}) => {
  switch (data.eventType) {
    case socketEvents.CONNECT:
      socket = connect(data.payload)
      timer = setInterval(() => send({command: socketMessages.READY}), 3000)
      break
    case socketEvents.DISCONNECT:
      clearInterval(timer)
      disconnect(socket)
      break
    case socketEvents.MESSAGE:
      send(data.payload)
      break
    default:
      console.log("Unknown message", data)
  }
})

function send(message) {
  if (connected)
    socket.send(JSON.stringify(message))
}

const messageStart = "{ \"eventType\": \"" + socketEvents.MESSAGE + "\", \"payload\": "

function connect({token, root}) {
  console.log("Received connect request", root)
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
      const data = preProcess(evt.data)
      postMessage(messageStart + data + "}")
    } catch (e) {
      console.log("Invalid message from server", evt.data)
    }
  }
  return socket
}

/**
 * Processor-heavy preprocessing we want to do on the incoming message
 * prior to transmitting to the main thread.
 */
function preProcess(data) {
  const obj = JSON.parse(data)
  switch (obj.nature) {

    case socketMessages.ORDERBOOK:

      const ORDERBOOK_SIZE = 16
      const orderBook = obj.data.orderBook
      var modified = false
      if (orderBook.asks.length > ORDERBOOK_SIZE) {
        orderBook.asks = orderBook.asks.slice(0, 16)
        modified = true
      }
      if (orderBook.bids.length > ORDERBOOK_SIZE) {
        orderBook.bids = orderBook.bids.slice(0, 16)
        modified = true
      }
      if (modified) {
        return JSON.stringify(obj)
      } else {
        return data
      }

    case socketMessages.TRADE_HISTORY:

      obj.data.trades = obj.data.trades.reverse()
      return JSON.stringify(obj)

    default:
      return data
  }
}

function disconnect(socket) {
  console.log("Received disconnect request")
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